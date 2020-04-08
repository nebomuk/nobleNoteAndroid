package com.taiko.noblenote.editor

import android.content.Context
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.textChanges
import com.taiko.noblenote.R
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.android.synthetic.main.toolbar_find_in_text.*
import kotlinx.android.synthetic.main.toolbar_find_in_text.view.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription

/**
 * handles visib
 */
class FindInTextToolbarController(val activity : EditorActivity) {

    private val mCompositeSubscription : CompositeSubscription = CompositeSubscription();
    private val mFindHighlighter: FindHighlighter


    init {

        val itemFindInText = activity.findViewById<Toolbar>(R.id.toolbar).menu.add(R.string.find_in_text)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
        itemFindInText.setOnMenuItemClickListener {
            showToolbarWithoutMove();
            true;
        }
        mCompositeSubscription += activity.toolbar_find_in_text.toolbar_find_in_text_close.clicks()
                .subscribe {
                    hideToolbar()
                }


        mFindHighlighter = FindHighlighter(editText = activity.editor_edit_text,
                toolbarEditText = activity.toolbar_find_in_text.toolbar_find_in_text_edit_text,
                scrollView = activity.editor_scroll_view)

        fun updateArrows()
        {
            setArrowDownEnabled(mFindHighlighter.hasNext());
            setArrowUpEnabled(mFindHighlighter.hasPrevious());
        }

        mCompositeSubscription+= activity.editor_edit_text.textChanges().subscribe {
            mFindHighlighter.onEditorTextChanged();
            mFindHighlighter.highlightWithoutScroll();
            updateArrows();
        }

        mCompositeSubscription += activity.toolbar_find_in_text.toolbar_find_in_text_edit_text.textChanges().subscribe {
            mFindHighlighter.mSearchString = it.toString();
            mFindHighlighter.highlight();
            updateArrows()
        }


        mCompositeSubscription += activity.toolbar_find_in_text.arrow_down.clicks().subscribe {
            mFindHighlighter.moveNext()
            mFindHighlighter.highlight();
            updateArrows();
        }
        mCompositeSubscription += activity.toolbar_find_in_text.arrow_up.clicks().subscribe {
            mFindHighlighter.movePrevious()
            mFindHighlighter.highlight();
            updateArrows();
        }

    }

    private fun setArrowDownEnabled(b : Boolean)
    {
        activity.toolbar_find_in_text.arrow_down.isEnabled = b;
    }
    private fun setArrowUpEnabled(b : Boolean)
    {
        activity.toolbar_find_in_text.arrow_up.isEnabled = b;
    }

    private fun showToolbarWithoutMove() {
        activity.toolbar_find_in_text.visibility = View.VISIBLE
        activity.toolbar_find_in_text_edit_text.toolbar_find_in_text_edit_text.requestFocus()
        // does not always work
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(activity.toolbar_find_in_text_edit_text, 0)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public fun hideToolbar()
    {
        clearFindText()
        activity.toolbar_find_in_text.visibility = View.INVISIBLE
        mFindHighlighter.clearHighlight()

        val imm =  activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
        imm.hideSoftInputFromWindow(activity.toolbar_find_in_text.toolbar_find_in_text_edit_text.windowToken, 0);
    }

    fun showToolbar()
    {
        showToolbarWithoutMove();
        mFindHighlighter.moveNext();
    }

    private fun clearFindText() {
        activity.toolbar_find_in_text.toolbar_find_in_text_edit_text.text.clear();
    }

    /**
     * @return true when handled, false when super class should be called
     */
    fun onBackPressed() : Boolean
    {

        return true;
    }

}

