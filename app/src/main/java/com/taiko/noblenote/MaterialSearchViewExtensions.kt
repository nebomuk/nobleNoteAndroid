package com.taiko.noblenote

import com.miguelcatalan.materialsearchview.MaterialSearchView

import rx.Observable
import rx.Subscriber
import rx.android.MainThreadSubscription

public class MaterialSearchViewQueryTextChangesOnSubscribe(val view: MaterialSearchView) : Observable.OnSubscribe<CharSequence> {

    override fun call(subscriber: Subscriber<in CharSequence>) {

        val watcher = object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextChange(s: String): Boolean {
                if (!subscriber.isUnsubscribed) {
                    subscriber.onNext(s)
                    return true
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
        }

        subscriber.add(object : MainThreadSubscription() {
            override fun onUnsubscribe() {
                view.setOnQueryTextListener(null)
            }
        })

        view.setOnQueryTextListener(watcher)

        // Emit initial value.
        //subscriber.onNext(view.getQuery());
    }
}

fun MaterialSearchView.queryTextChanges() : Observable<CharSequence>
{
    return Observable.create(MaterialSearchViewQueryTextChangesOnSubscribe(this))
}
