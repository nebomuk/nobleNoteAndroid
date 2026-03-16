package de.blogspot.noblenoteandroid.extensions


import androidx.databinding.ObservableList
import rx.Observable
import rx.subscriptions.Subscriptions

fun <T> ObservableList<T>.toRxObservable(): Observable<List<T>> {
    return Observable.create { subscriber ->
        subscriber.onNext(this)
        val listener = object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
            override fun onChanged(ts: ObservableList<T>) {
                subscriber.onNext(ts)
            }

            override fun onItemRangeChanged(ts: ObservableList<T>, i: Int, i1: Int) {
                subscriber.onNext(ts)
            }

            override fun onItemRangeInserted(ts: ObservableList<T>, i: Int, i1: Int) {
                subscriber.onNext(ts)
            }

            override fun onItemRangeMoved(ts: ObservableList<T>, i: Int, i1: Int, i2: Int) {
                subscriber.onNext(ts)
            }

            override fun onItemRangeRemoved(ts: ObservableList<T>, i: Int, i1: Int) {
                subscriber.onNext(ts)
            }
        }
        addOnListChangedCallback(listener)

        subscriber.add(Subscriptions.create { removeOnListChangedCallback(listener) })
    }

}
