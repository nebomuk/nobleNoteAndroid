package com.taiko.noblenote

import android.support.v4.view.GestureDetectorCompat
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import rx.Observable
import rx.Observer
import rx.lang.kotlin.add

/**
 * Created by fabdeuch on 05.10.2016.
 */
fun RecyclerView.itemClicks() : Observable<Int>
{
    return Observable.create<Int> {
        val listener = ClickDetectorItemTouchListener(this, it)
        this.addOnItemTouchListener(listener)
        it.add { this.removeOnItemTouchListener(listener) }

    }
}

class ClickDetectorItemTouchListener(val recyclerView: RecyclerView, val listener: Observer<in Int>) : RecyclerView.OnItemTouchListener, GestureDetector.SimpleOnGestureListener() {

    val gestureDetector: GestureDetectorCompat = GestureDetectorCompat(recyclerView.context, this)


    init {
        gestureDetector.setIsLongpressEnabled(true)
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }

    override fun onInterceptTouchEvent(recyclerView: RecyclerView?, e: MotionEvent?) = gestureDetector.onTouchEvent(e)

    override fun onTouchEvent(recyclerView: RecyclerView?, e: MotionEvent?) { }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {

        if (e != null) {

            val view = recyclerView.findChildViewUnder(e.x, e.y)
            val position = recyclerView.getChildAdapterPosition(view)
            val id = recyclerView.getChildItemId(view)

            if (id >= 0) {
                listener.onNext(position)
            }
            //listener.onNext(recyclerView, view, position, id)
        }

        return false
    }
}
