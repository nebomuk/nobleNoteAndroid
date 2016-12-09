package com.taiko.noblenote

import android.widget.EditText

/**
 *  @author Taiko
 *
 * text search
 */
class Finder constructor(editText : EditText)
{
    private val mEditText = editText

    public var searchString : String = ""


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