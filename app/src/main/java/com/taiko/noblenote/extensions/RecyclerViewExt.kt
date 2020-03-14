package com.taiko.noblenote.extensions

import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import com.taiko.noblenote.loggerFor
import rx.Observable
import rx.Observer
import rx.lang.kotlin.add

/**
 * remove ac cable from phone when clicking behaviour is "abnormal"
 */
fun RecyclerView.itemClicks() : Observable<Int>
{
    return Observable.create<Int> {
        val listener = ClickDetectorItemTouchListener(this, it)
        this.addOnItemTouchListener(listener)
        it.add { this.removeOnItemTouchListener(listener) }

    }
}

fun RecyclerView.itemLongClicks() : Observable<Int>
{
    return Observable.create<Int> {
        val listener = ClickDetectorItemLongPressListener(this, it)
        this.addOnItemTouchListener(listener)
        it.add { this.removeOnItemTouchListener(listener) }

    }
}


class ClickDetectorItemTouchListener(val recyclerView: RecyclerView,
                                     val listener: Observer<in Int>

) : RecyclerView.OnItemTouchListener, GestureDetector.SimpleOnGestureListener() {

    private  val log = loggerFor();

    private val gestureDetector: GestureDetectorCompat = GestureDetectorCompat(recyclerView.context, this)

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }

    override fun onInterceptTouchEvent(recyclerView: RecyclerView, e: MotionEvent) = gestureDetector.onTouchEvent(e)

    override fun onTouchEvent(recyclerView: RecyclerView, e: MotionEvent) { }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {



        if (e != null) {

            val view = recyclerView.findChildViewUnder(e.x, e.y)
            val position = recyclerView.getChildAdapterPosition(view!!)
            val id = recyclerView.getChildItemId(view)

            log.i("onSingleTapUp position: " + position)

            if (position >= 0) {
                    listener.onNext(position)

            }
            //listener.onNext(recyclerView, view, position, id)
        }

        return true
    }
}

class ClickDetectorItemLongPressListener(val recyclerView: RecyclerView,
                                         val listener: Observer<in Int>

) : RecyclerView.OnItemTouchListener, GestureDetector.SimpleOnGestureListener() {

    val gestureDetector: GestureDetectorCompat = GestureDetectorCompat(recyclerView.context, this)


    init {
        gestureDetector.setIsLongpressEnabled(true)
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }

    override fun onInterceptTouchEvent(recyclerView: RecyclerView, e: MotionEvent) = gestureDetector.onTouchEvent(e)

    override fun onTouchEvent(recyclerView: RecyclerView, e: MotionEvent) { }

    override fun onLongPress(e: MotionEvent?) {
        if (e != null) {

            val view = recyclerView.findChildViewUnder(e.x, e.y)
            val position = recyclerView.getChildAdapterPosition(view!!)
            val id = recyclerView.getChildItemId(view)

            if (position >= 0) {
                listener.onNext(position)
            }
        }
    }
}
