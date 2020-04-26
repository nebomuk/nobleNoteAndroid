package com.taiko.noblenote.editor

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.textChanges
import com.taiko.noblenote.R
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar_find_in_text.*
import kotlinx.android.synthetic.main.toolbar_find_in_text.view.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription

/**
 * handles visib
 */
class FindInTextToolbarController(val fragment : EditorFragment) {

    private val mCompositeSubscription : CompositeSubscription = CompositeSubscription();
    private val mFindHighlighter: FindHighlighter


    init {

        val itemFindInText = fragment.toolbar.menu.add(R.string.find_in_text)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
        itemFindInText.setOnMenuItemClickListener {
            showToolbarWithoutMove();
            true;
        }
        mCompositeSubscription += fragment.toolbar_find_in_text.toolbar_find_in_text_close.clicks()
                .subscribe {
                    hideToolbar()
                }


        mFindHighlighter = FindHighlighter(editText = fragment.editor_edit_text,
                toolbarEditText = fragment.toolbar_find_in_text.toolbar_find_in_text_edit_text,
                scrollView = fragment.editor_scroll_view)

        fun updateArrows()
        {
            setArrowDownEnabled(mFindHighlighter.hasNext());
            setArrowUpEnabled(mFindHighlighter.hasPrevious());
        }

        mCompositeSubscription+= fragment.editor_edit_text.textChanges().subscribe {
            mFindHighlighter.onEditorTextChanged();
            mFindHighlighter.highlightWithoutScroll();
            updateArrows();
        }

        mCompositeSubscription += fragment.toolbar_find_in_text.toolbar_find_in_text_edit_text.textChanges().subscribe {
            mFindHighlighter.mSearchString = it.toString();
            mFindHighlighter.highlight();
            updateArrows()
        }


        mCompositeSubscription += fragment.toolbar_find_in_text.arrow_down.clicks().subscribe {
            mFindHighlighter.moveNext()
            mFindHighlighter.highlight();
            updateArrows();
        }
        mCompositeSubscription += fragment.toolbar_find_in_text.arrow_up.clicks().subscribe {
            mFindHighlighter.movePrevious()
            mFindHighlighter.highlight();
            updateArrows();
        }

    }

    private fun setArrowDownEnabled(b : Boolean)
    {
        fragment.toolbar_find_in_text.arrow_down.isEnabled = b;
    }
    private fun setArrowUpEnabled(b : Boolean)
    {
        fragment.toolbar_find_in_text.arrow_up.isEnabled = b;
    }

    private fun showToolbarWithoutMove() {
        fragment.toolbar_find_in_text.visibility = View.VISIBLE
        showKeyboard()
    }

    private fun showKeyboard() {
        fragment.toolbar_find_in_text_edit_text.requestFocus()
        // delay is required
        val h = Handler()
        h.postDelayed({
            val imm = fragment.requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(fragment.toolbar_find_in_text_edit_text, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    public fun hideToolbar()
    {
        clearFindText()
        fragment.toolbar_find_in_text.visibility = View.INVISIBLE
        mFindHighlighter.clearHighlight()

        val imm =  fragment.requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
        imm.hideSoftInputFromWindow(fragment.toolbar_find_in_text.toolbar_find_in_text_edit_text.windowToken, 0);
    }

    fun showToolbar()
    {
        showToolbarWithoutMove();
        mFindHighlighter.moveNext();
    }

    private fun clearFindText() {
        fragment.toolbar_find_in_text.toolbar_find_in_text_edit_text.text.clear();
    }

}

