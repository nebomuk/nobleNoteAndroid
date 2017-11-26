package com.taiko.noblenote

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat


fun Drawable?.setTintCompat(context : Context, @ColorInt color : Int)
{
    if(this != null) {
        DrawableCompat.setTint(this, ContextCompat.getColor(context, color));
    }
}
