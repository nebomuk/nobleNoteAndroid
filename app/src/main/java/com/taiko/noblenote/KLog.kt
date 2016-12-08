package com.taiko.noblenote

import android.util.Log


object KLog {

    @JvmStatic fun v(message: String) {
        Log.v(this.javaClass.name, message)
    }

    @JvmStatic fun d(message: String) {
        Log.d(this.javaClass.name, message)
    }

    @JvmStatic fun i(message: String) {
        Log.i(this.javaClass.name, message)
    }

    @JvmStatic fun w(message: String) {
        Log.w(this.javaClass.name, message)
    }

    @JvmStatic fun e(message: String) {
        Log.e(this.javaClass.name, message)
    }

    @JvmStatic fun wtf(message: String) {
        Log.wtf(this.javaClass.name, message)
    }
    @JvmStatic fun v(message: String, exception : Throwable) {
        Log.v(this.javaClass.name, message, exception)
    }

    @JvmStatic fun d(message: String, exception : Throwable) {
        Log.d(this.javaClass.name, message, exception)
    }

    @JvmStatic fun i(message: String, exception : Throwable) {
        Log.i(this.javaClass.name, message, exception)
    }

    @JvmStatic fun w(message: String, exception : Throwable) {
        Log.w(this.javaClass.name, message, exception)
    }


    @JvmStatic fun e(message: String, exception : Throwable) {
        Log.e(this.javaClass.name, message, exception)
    }

    @JvmStatic fun wtf(message: String, exception : Throwable) {
        Log.wtf(this.javaClass.name, message, exception)
    }
}