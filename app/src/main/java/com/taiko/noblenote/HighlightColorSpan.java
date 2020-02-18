package com.taiko.noblenote;

import androidx.annotation.ColorInt;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

/**
 * based on BackgroundColorSpan,
 * used to highlight text in search
 */
public class HighlightColorSpan extends CharacterStyle
        implements UpdateAppearance
{

    private final int mColor;

    public HighlightColorSpan(@ColorInt  int color) {
        mColor = color;
    }

    public int getBackgroundColor() {
        return mColor;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.bgColor = mColor;
    }
}
