package de.blogspot.noblenoteandroid.editor

import android.content.Context
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.textChanges
import de.blogspot.noblenoteandroid.R
import de.blogspot.noblenoteandroid.databinding.ToolbarFindInTextBinding
import de.blogspot.noblenoteandroid.extensions.MapWithIndex
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription

/**
 * handles visib
 */
class FindInTextToolbarController(editor : EditText,
                                  scrollView: ScrollView,
                                  toolbar : Toolbar,
                                  private val binding : ToolbarFindInTextBinding) {

    private val mCompositeSubscription : CompositeSubscription = CompositeSubscription();
    private val mFindHighlighter: FindHighlighter


    private val context get() = binding.toolbar.context;

    init {

        val itemFindInText = toolbar.menu.add(R.string.find_in_text)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
        itemFindInText.setOnMenuItemClickListener {
            showToolbarWithoutMove();
            true;
        }
        mCompositeSubscription += binding.close.clicks()
                .subscribe {
                    hideToolbar()
                }


        mFindHighlighter = FindHighlighter(editText = editor,
                toolbarEditText = binding.searchInput,
                scrollView = scrollView)

        fun updateArrows()
        {
            setArrowDownEnabled(mFindHighlighter.hasNext());
            setArrowUpEnabled(mFindHighlighter.hasPrevious());
        }


        mCompositeSubscription+=
                editor.textChanges().filter { it.isNotEmpty() }
                        .compose(MapWithIndex.instance())
                .subscribe {
            mFindHighlighter.onEditorTextChanged();
                    if(it.index() == 0L)
                    {
                        mFindHighlighter.highlight();
                    }
                    else
                    {
                        mFindHighlighter.highlightWithoutScroll()
                    }
            updateArrows();
        }

        mCompositeSubscription += binding.searchInput.textChanges().subscribe {
            mFindHighlighter.mSearchString = it.toString();
            mFindHighlighter.highlight();
            updateArrows()
        }


        mCompositeSubscription += binding.arrowDown.clicks().subscribe {
            mFindHighlighter.moveNext()
            mFindHighlighter.highlight();
            updateArrows();
        }
        mCompositeSubscription += binding.arrowUp.clicks().subscribe {
            mFindHighlighter.movePrevious()
            mFindHighlighter.highlight();
            updateArrows();
        }

    }

    private fun setArrowDownEnabled(b : Boolean)
    {
        binding.arrowDown.isEnabled = b;
    }
    private fun setArrowUpEnabled(b : Boolean)
    {
        binding.arrowUp.isEnabled = b;
    }

    private fun showToolbarWithoutMove() {
        binding.toolbar.visibility = View.VISIBLE
        showKeyboard()
    }

    private fun showKeyboard() {
        binding.searchInput.requestFocus()
        // delay is required
        val h = Handler()
        h.postDelayed({
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    public fun hideToolbar()
    {
        clearFindText()
        binding.toolbar.visibility = View.INVISIBLE
        mFindHighlighter.clearHighlight()

        val imm =  context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
        imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0);
    }

    fun showToolbar()
    {
        showToolbarWithoutMove();
        mFindHighlighter.moveNext();
    }

    private fun clearFindText() {
        binding.searchInput.text.clear();
    }

}

