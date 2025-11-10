package com.blogspot.noblenoteandroid.editor;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

import java.util.LinkedList;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * A generic undo/redo implementation for TextViews.
 *
 * based on https://code.google.com/p/android/issues/attachmentText?id=6458&aid=64580123000&name=TextViewUndoRedo.java&token=ABZ6GAcCToH1Lnjgqfh9vh8jI6yMrz18Ww%3A1481276173570
 *
 * class is not serializable because undo redo does not work properly with Spannables
 */
public class TextViewUndoRedo {

    /**
     * Is undo/redo being performed? This member signals if an undo/redo
     * operation is currently being performed. Changes in the text during
     * undo/redo are not recorded because it would mess up the undo history.
     */
    private boolean mIsUndoOrRedo = false;

    /**
     * The edit history.
     */
    private EditHistory mEditHistory;

    /**
     * The change listener.
     */
    private EditTextChangeListener mChangeListener;

    /**
     * The edit text.
     */
    private TextView mTextView;
    private PublishSubject<Boolean> mUndoChangedSubject = PublishSubject.create();
    private PublishSubject<Boolean> mRedoChangedSubject = PublishSubject.create();

    // =================================================================== //

    /**
     * Create a new TextViewUndoRedo and attach it to the specified TextView.
     *
     * @param textView
     *            The text view for which the undo/redo is implemented.
     */
    public TextViewUndoRedo(TextView textView) {
        mTextView = textView;
        mEditHistory = new EditHistory();
        mChangeListener = new EditTextChangeListener();
        mTextView.addTextChangedListener(mChangeListener);

    }

    // =================================================================== //

    /**
     * Disconnect this undo/redo from the text view.
     */
    public void disconnect() {
        mTextView.removeTextChangedListener(mChangeListener);
    }

    /**
     * Set the maximum history size. If size is negative, then history size is
     * only limited by the device memory.
     */
    public void setMaxHistorySize(int maxHistorySize) {
        mEditHistory.setMaxHistorySize(maxHistorySize);
    }

    /**
     * Clear history.
     */
    public void clearHistory() {
        mEditHistory.clear();
    }

    /**
     * Can undo be performed?
     */
    private boolean getCanUndo() {
        return (mEditHistory.mmPosition > 1);
    }

    /**
     * Perform undo.
     */
    public void undo() {
        if(!getCanUndo())
            return;

        EditItem edit = mEditHistory.getPrevious();
        if (edit == null) {
            return;
        }

        Editable text = mTextView.getEditableText();
        int start = edit.mmStart;
        int end = start + (edit.mmAfter != null ? edit.mmAfter.length() : 0);

        mIsUndoOrRedo = true;
        text.replace(start, end, edit.mmBefore);
        mIsUndoOrRedo = false;

        // This will get rid of underlines inserted when editor tries to come
        // up with a suggestion.
        for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) {
            text.removeSpan(o);
        }

        Selection.setSelection(text, edit.mmBefore == null ? start
                : (start + edit.mmBefore.length()));
    }

    /**
     * Can redo be performed?
     */
    private boolean getCanRedo() {
        return (mEditHistory.mmPosition < mEditHistory.mmHistory.size());
    }

    /**
     * Perform redo.
     */
    public void redo() {
        if(!getCanRedo())
            return;

        EditItem edit = mEditHistory.getNext();
        if (edit == null) {
            return;
        }

        Editable text = mTextView.getEditableText();
        int start = edit.mmStart;
        int end = start + (edit.mmBefore != null ? edit.mmBefore.length() : 0);

        mIsUndoOrRedo = true;
        text.replace(start, end, edit.mmAfter);
        mIsUndoOrRedo = false;

        // This will get rid of underlines inserted when editor tries to come
        // up with a suggestion.
        for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) {
            text.removeSpan(o);
        }

        Selection.setSelection(text, edit.mmAfter == null ? start
                : (start + edit.mmAfter.length()));
    }


    // =================================================================== //

    /**
     * Keeps track of all the edit history of a text.
     */
    private final static class EditHistory {

        /**
         * The position from which an EditItem will be retrieved when getNext()
         * is called. If getPrevious() has not been called, this has the same
         * value as mmHistory.size().
         */
        private int mmPosition = 0;

        /**
         * Maximum undo history size.
         */
        private int mmMaxHistorySize = -1;

        /**
         * The list of edits in chronological order.
         */
        private final LinkedList<EditItem> mmHistory = new LinkedList<EditItem>();

        /**
         * Clear history.
         */
        private void clear() {
            mmPosition = 0;
            mmHistory.clear();
        }

        /**
         * Adds a new edit operation to the history at the current position. If
         * executed after a call to getPrevious() removes all the future history
         * (elements with positions >= current history position).
         */
        private void add(EditItem item) {
            while (mmHistory.size() > mmPosition) {
                mmHistory.removeLast();
            }
            mmHistory.add(item);
            mmPosition++;

            if (mmMaxHistorySize >= 0) {
                trimHistory();
            }
        }

        /**
         * Set the maximum history size. If size is negative, then history size
         * is only limited by the device memory.
         */
        private void setMaxHistorySize(int maxHistorySize) {
            mmMaxHistorySize = maxHistorySize;
            if (mmMaxHistorySize >= 0) {
                trimHistory();
            }
        }

        /**
         * Trim history when it exceeds max history size.
         */
        private void trimHistory() {
            while (mmHistory.size() > mmMaxHistorySize) {
                mmHistory.removeFirst();
                mmPosition--;
            }

            if (mmPosition < 0) {
                mmPosition = 0;
            }
        }

        /**
         * Traverses the history backward by one position, returns and item at
         * that position.
         */
        private EditItem getPrevious() {
            if (mmPosition == 0) {
                return null;
            }
            mmPosition--;
            return mmHistory.get(mmPosition);
        }

        /**
         * Traverses the history forward by one position, returns and item at
         * that position.
         */
        private EditItem getNext() {
            if (mmPosition >= mmHistory.size()) {
                return null;
            }

            EditItem item = mmHistory.get(mmPosition);
            mmPosition++;
            return item;
        }
    }

    /**
     * Represents the changes performed by a single edit operation.
     */
    private final static class EditItem {
        private final int mmStart;
        private final CharSequence mmBefore;
        private final CharSequence mmAfter;

        /**
         * Constructs EditItem of a modification that was applied at position
         * start and replaced CharSequence before with CharSequence after.
         */
        EditItem(int start, CharSequence before, CharSequence after) {
            mmStart = start;
            mmBefore = before;
            mmAfter = after;
        }
    }

    /**
     * @return true if can undo, false otherwise
     */
    public Observable<Boolean> canUndoChanged()
    {
        return mUndoChangedSubject.startWith(getCanUndo()).distinctUntilChanged();
    }

    /*
     * @return true if can redo, false otherwise
     */
    public Observable<Boolean> canRedoChanged()
    {
        return mRedoChangedSubject.startWith(getCanRedo()).distinctUntilChanged();
    }

    /**
     * Class that listens to changes in the text.
     */
    private final class EditTextChangeListener implements TextWatcher {

        /**
         * The text that will be removed by the change event.
         */
        private CharSequence mBeforeChange;

        private boolean textChanged = false;

        /**
         * The text that was inserted by the change event.
         */
        private CharSequence mAfterChange;

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            if (mIsUndoOrRedo) {
                return;
            }

            mBeforeChange = s.subSequence(start, start + count);
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (mIsUndoOrRedo) {
                return;
            }

            // detect if setText was called by the viewModel after an configuration change
            if((count > 1 || count == 0) && textChanged == false && start == 0 && before == 0)
            {
                return;
            }

            textChanged = true;

            mAfterChange = s.subSequence(start, start + count);
            mEditHistory.add(new EditItem(start, mBeforeChange, mAfterChange));

        }

        public void afterTextChanged(Editable s) {
            invokeUndoRedoListeners();

        }
    }

    private void invokeUndoRedoListeners()
    {
        mUndoChangedSubject.onNext(getCanUndo());
        mRedoChangedSubject.onNext(getCanRedo());
    }
}

