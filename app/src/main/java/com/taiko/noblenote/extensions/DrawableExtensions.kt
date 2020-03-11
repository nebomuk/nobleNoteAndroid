package com.taiko.noblenote.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat


fun Drawable?.setTintCompat(context : Context, @ColorInt color : Int)
{
    if(this != null) {
        DrawableCompat.setTint(this, ContextCompat.getColor(context, color));
    }
}
