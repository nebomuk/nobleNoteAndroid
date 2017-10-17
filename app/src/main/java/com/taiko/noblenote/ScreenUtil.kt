package com.taiko.noblenote

import android.content.Context
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
}