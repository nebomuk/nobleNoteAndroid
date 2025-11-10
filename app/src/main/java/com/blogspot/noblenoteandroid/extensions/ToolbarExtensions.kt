package com.blogspot.noblenoteandroid.extensions

import android.annotation.SuppressLint
import android.text.Layout
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding.view.RxView
import com.blogspot.noblenoteandroid.util.loggerFor
import rx.android.schedulers.AndroidSchedulers


@SuppressLint("SetTextI18n")
public fun Toolbar.setTitleAndModified(defaultTitle : String, isModified : Boolean) {
    val log = loggerFor()

    if(!isModified)
    {
        this.title = defaultTitle;
        return;
    }

    this.title = defaultTitle;

    if(this.title.isNullOrEmpty())
        return;

    // handles ellipsis correctly
    // getTitle() is not in sync with the textView after restore, this re-syncs it as a side effect
    if(!this.title.endsWith("*"))
    {
        val textView = this.getTitleTextView();

        val layout = textView?.layout;

        if (layout != null)
        {
            this.addModifiedIndicatorToEllipsis(textView,layout);
        }
        else
        {
            RxView.preDraws(textView,{true}) // fix not yet attached to layout
                    .takeUntil { textView?.layout != null } // fix layout still null in onDraw
                    .last()
                    .observeOn(AndroidSchedulers.mainThread()) // fix not working when not called on the next ui cycle
                    .subscribe {
                        try {
                            val latestLayout = textView.layout;
                            this@setTitleAndModified.addModifiedIndicatorToEllipsis(textView, latestLayout);
                        } catch (e: Exception) {
                            log.d("failed to set title as modified in view tree observer",e);
                        }
                    }
        }

    }


}

private fun Toolbar.addModifiedIndicatorToEllipsis(textView: TextView,layout: Layout)
{
    if( layout.getEllipsisStart(0) > 0) {

        //textView.post {
            val customSuffix = "…*"
            val newText = textView.text.removeRange(layout.getEllipsisStart(-1) - customSuffix.length, textView.text.length)
            this.title = String.format("%s%s", newText, customSuffix)
        //}
    }
    else
    {
        this.title = "${this.title}*"
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

public fun Toolbar.getMenuItems() : Sequence<MenuItem>
{
    if(this.menu == null)
    {
        return emptySequence();
    }
    return sequence {
        for (i in 0 until menu.size()) {
            yield(menu.getItem(i));
        }
    }
}