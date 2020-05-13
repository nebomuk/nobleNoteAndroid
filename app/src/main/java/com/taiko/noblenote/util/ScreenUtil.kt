package com.taiko.noblenote.util

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue





/**
 * Created by Taiko
 */
object ScreenUtil {

    @JvmStatic
    fun dpToPx(context : Context, dp : Int): Int {
        val r = context.getResources()
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14f, r.getDisplayMetrics())
        return px.toInt();
    }

    @JvmStatic
    fun isTablet(context: Context): Boolean {
        return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }
}