package de.blogspot.noblenoteandroid.editor

import android.text.Spannable
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.blogspot.noblenoteandroid.R
import de.blogspot.noblenoteandroid.extensions.indicesOf


/**
 *  @author Taiko
 *
 * text search
 */
class FindHighlighter constructor(val editText : EditText, val toolbarEditText : EditText, val scrollView: ScrollView)
{
    var mSearchString: String = ""
    set(value)
    {
        if(value != mSearchString)
        {
            mIndices = editText.text.indicesOf(value);
            field = value;
        }
    }

    private var mCurrentIndicesIndex = 0 // the current index in the list of indices


    private val highlightSpan: HighlightColorSpan = HighlightColorSpan(ContextCompat.getColor(toolbarEditText.context,R.color.findInTextHighlight));

    private var mIndices = emptyList<Int>(); // a list of indexes where the searchString can be found in the text

    init {
        // handle enter press
        toolbarEditText.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override  fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                    moveNext()
                    return true
                }
                return false
            }
        })
    }

    // should be called when the editor text has been changed
    // updates sthe search indices
    fun onEditorTextChanged()
    {
        mIndices = editText.text.indicesOf(mSearchString);
    }



    fun clearHighlight()
    {

        // TODO check if there are issues with multiple foreground color spans
        // https://stackoverflow.com/questions/34631851/multiple-foregroundcolorspan-on-editable-issue-on-android-6-0
        val editable = editText.text;
        val spans = editable.getSpans(0, editable.length, HighlightColorSpan::class.java)
        for (span in spans) {
            editable.removeSpan(span)
        }
    }

    fun hasNext() : Boolean
    {
        return mCurrentIndicesIndex < mIndices.size -1;
    }

    fun hasPrevious() : Boolean
    {
        return  mCurrentIndicesIndex > 0;
    }

    fun highlight()
    {
        highlightInternal { scrollToIndex(it)  }
    }

    fun highlightWithoutScroll()
    {
        highlightInternal {  }
    }

    private fun highlightInternal(scrollFunction : (index : Int)-> Unit)
    {
        if(mSearchString.isBlank() || mIndices.isEmpty()) // cannot highlight zero length, so simply clear the highlighting
        {
            clearHighlight();
        }
        else if (mCurrentIndicesIndex >= 0 && mCurrentIndicesIndex < mIndices.size)
        {
            val index = mIndices[mCurrentIndicesIndex];
            editText.text.setSpan(highlightSpan,index , index + mSearchString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            if(toolbarEditText.visibility == View.VISIBLE)
            {
                scrollFunction(index)
            }
        }
    }



    fun movePrevious()
    {
        if(hasPrevious())
        {
            --mCurrentIndicesIndex;
        }
    }

    fun moveNext()
    {
        if(hasNext())
        {
            ++mCurrentIndicesIndex;
        }
    }

    private fun scrollToIndex(index : Int)
    {
        scrollView.post {

            val y = editText.layout.getLineTop(editText.layout.getLineForOffset(index))
            scrollView.smoothScrollTo(0,y)

        }
    }

    // moves to the nearest index of the search word using the specified text position
    @Deprecated("this feature is not longer used, because it's confusing when the search starts from the cursor position (words before the cursor might not be found")
    fun moveSearchStart(textPos : Int)
    {

        val closestValue = mIndices.filter {textPos <= it }.minBy { Math.abs(textPos - it) }
        if(closestValue != null) {
            val index = mIndices.indexOf(closestValue);
            if(index != -1) {
                mCurrentIndicesIndex = index;
            }
        }
    }



}