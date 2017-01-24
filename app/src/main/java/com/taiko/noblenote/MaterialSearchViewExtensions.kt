package com.taiko.noblenote

import android.text.Editable
import android.widget.EditText
import com.miguelcatalan.materialsearchview.MaterialSearchView
import rx.Observable
import rx.Subscriber
import rx.android.MainThreadSubscription


public class MaterialSearchViewQueryTextChangesOnSubscribe(val view: MaterialSearchView) : Observable.OnSubscribe<MaterialSearchViewQueryTextChangesOnSubscribe.QueryResult> {

    data class QueryResult(val text : CharSequence, val isSubmit : Boolean) // if sSubmit is true, the text is the submitted text, else its updated text

    override fun call(subscriber: Subscriber<in QueryResult>) {

        val watcher = object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextChange(s: String): Boolean {
                if (!subscriber.isUnsubscribed) {
                    subscriber.onNext(QueryResult(s,false))
                    return true
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                if (!subscriber.isUnsubscribed) {
                    subscriber.onNext(QueryResult(query,true))
                    return true
                }
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

val  MaterialSearchView.queryText : Editable
get() {
        val et = this.findViewById(R.id.searchTextView) as EditText
        return et.text;
}



fun MaterialSearchView.queryTextChanges() : Observable<MaterialSearchViewQueryTextChangesOnSubscribe.QueryResult>
{
    return Observable.create(MaterialSearchViewQueryTextChangesOnSubscribe(this))
}

