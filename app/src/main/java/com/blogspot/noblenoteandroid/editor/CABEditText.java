package com.blogspot.noblenoteandroid.editor;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

/**
 * Created by taiko on 05.03.15.
 *
 * based on https://code.google.com/p/android/issues/detail?id=23381#makechanges
 *
 * works arround an android bug that makes it impossible to use the overflow menu in the contextual action bar
 */


public class CABEditText extends AppCompatEditText {

    private boolean shouldWindowFocusWait;

    public CABEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CABEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CABEditText(Context context) {
        super(context);
    }

    public void setWindowFocusWait(boolean shouldWindowFocusWait) {
        this.shouldWindowFocusWait = shouldWindowFocusWait;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if(!shouldWindowFocusWait) {
            super.onWindowFocusChanged(hasWindowFocus);
        }
    }
}
