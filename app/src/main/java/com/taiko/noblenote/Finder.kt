package com.taiko.noblenote

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView



/**
 *  @author Taiko
 *
 * text search
 */
class Finder constructor(editText : EditText)
{
    private val mEditText = editText

    public var searchString : String = ""


    init {
        editText.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override  fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    selectNext()
                    return true
                }
                return false
            }
        })
    }


    fun selectPrevious() : Boolean
    {
        val localStart = if (mEditText.hasSelection()) mEditText.selectionStart-1 else mEditText.text.length-1;
        val index = mEditText.text.lastIndexOf(searchString,localStart,ignoreCase = true)
        if(index == -1)
        {
            return false;
        }
        else
        {
            mEditText.requestFocus()
            mEditText.setSelection(index,index + searchString.length)
            return true;
        }
    }

    fun selectNext() : Boolean
    {
        val localStart = if (mEditText.hasSelection()) mEditText.selectionEnd else 0;
        val index = mEditText.text.indexOf(searchString,localStart,ignoreCase = true)
        if(index == -1)
        {
            return false;
        }
        else
        {
            mEditText.requestFocus()
            mEditText.setSelection(index,index + searchString.length)
            return true;
        }
    }


}