package com.taiko.noblenote.extensions

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import java.lang.UnsupportedOperationException

@SuppressLint("SetTextI18n")
public fun Toolbar.markTitleAsModified() {
    if(this.title.isNullOrEmpty())
        return;

    // handles ellipsis correctly
    // getTitle() is not in sync with the textView after restore, this re-syncs it as a side effect
    if(!this.title.endsWith("*"))
    {
        val textView = this.getTitleTextView();

        val layout = textView?.layout;
        if (layout != null && layout.getEllipsisStart(-1) > 0)
        {
            textView.post {
                val customSuffix = "…*"
                val newText = textView.text.removeRange(textView.layout.getEllipsisStart(-1) - customSuffix.length, textView.text.length)
                this.title = String.format("%s%s", newText, customSuffix)
            }
        }
        else
        {
            this.title = "${this.title}*"
        }
    }
}

public fun Toolbar.getTitleTextView(): TextView {
    for (i in 0 until childCount) {
        val view = getChildAt(i)
        if (view is TextView && view.text.contains(title)) // text already contains the *
        {
            return view;
        }
    }
    // FIXME change to error instead of exception
    throw UnsupportedOperationException("This toolbar does not contain a textview");
}