package de.blogspot.noblenoteandroid.extensions

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import rx.Observable
import rx.lang.kotlin.add

// RxJava 1 reactive streams extensions for Live Data
// currently, LiveData only supports conversion to RxJava2, which is already outdated as of april 2020
fun <T> LiveData<T>.toObservable() : Observable<T>
{
    return Observable.create<T> { subscriber ->

        val liveDataObserver = Observer<T> { subscriber.onNext(it)};
        this.observeForever(liveDataObserver)

        subscriber.add { Handler(Looper.getMainLooper()).post { this.removeObserver(liveDataObserver) } }

    }
}