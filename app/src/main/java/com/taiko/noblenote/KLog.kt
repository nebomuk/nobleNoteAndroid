package com.taiko.noblenote

import android.util.Log

// simple kotlin logging
object KLog {
    
    private val Tag = "";

    @JvmStatic fun v(message: String) {
        Log.v(Tag, message)
    }

    @JvmStatic fun d(message: String) {
        Log.d(Tag, message)
    }

    @JvmStatic fun i(message: String) {
        Log.i(Tag, message)
    }

    @JvmStatic fun w(message: String) {
        Log.w(Tag, message)
    }

    @JvmStatic fun e(message: String) {
        Log.e(Tag, message)
    }

    @JvmStatic fun wtf(message: String) {
        Log.wtf(Tag, message)
    }
    @JvmStatic fun v(message: String, exception : Throwable) {
        Log.v(Tag, message, exception)
    }

    @JvmStatic fun d(message: String, exception : Throwable) {
        Log.d(Tag, message, exception)
    }

    @JvmStatic fun i(message: String, exception : Throwable) {
        Log.i(Tag, message, exception)
    }

    @JvmStatic fun w(message: String, exception : Throwable) {
        Log.w(Tag, message, exception)
    }


    @JvmStatic fun e(message: String, exception : Throwable) {
        Log.e(Tag, message, exception)
    }

    @JvmStatic fun wtf(message: String, exception : Throwable) {
        Log.wtf(Tag, message, exception)
    }
}