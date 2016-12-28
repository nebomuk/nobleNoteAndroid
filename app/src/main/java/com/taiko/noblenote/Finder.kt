package com.taiko.noblenote

import android.graphics.Color
import android.text.Spannable
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView





/**
 *  @author Taiko
 *
 * text search
 */
class Finder constructor(val editText : EditText,val toolbarEditText : EditText, val scrollView: ScrollView)
{
    public var searchString : String = ""
    var currentIndex : Int = 0; // highlight Index
    private var highlightSpan: HighlightColorSpan = HighlightColorSpan(Color.YELLOW);

    init {
        toolbarEditText.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override  fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                    highlightNext()
                    return true
                }
                return false
            }
        })
    }

    fun clearHighlight()
    {
        editText.text.removeSpan(highlightSpan)
    }


    fun highlightPrevious() : Boolean
    {
        val localStart = if (currentIndex < editText.text.length-1) currentIndex-1 else editText.text.length-1;
        val index = editText.text.lastIndexOf(searchString,localStart,ignoreCase = true)
        if(index == -1)
        {
            return false;
        }
        else
        {
            editText.text.setSpan(highlightSpan,index,index + searchString.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            scrollToIndex(index)
            currentIndex = index;
            return true;
        }
    }

    fun highlightNext() : Boolean
    {
        val localStart = if (currentIndex > 0) currentIndex+ searchString.length else 0;
        val index = editText.text.indexOf(searchString,localStart,ignoreCase = true)
        if(index == -1)
        {
            return false;
        }
        else
        {
            editText.text.setSpan(highlightSpan,index,index + searchString.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            scrollToIndex(index)

            currentIndex = index;

            return true;
        }
    }

    fun scrollToIndex(index : Int)
    {
        scrollView.post {

            val y = editText.layout.getLineTop(editText.layout.getLineForOffset(index))
            scrollView.smoothScrollTo(0,y)

        }

    }


}