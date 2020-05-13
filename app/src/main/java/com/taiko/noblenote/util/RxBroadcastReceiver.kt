package com.taiko.noblenote.util


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import rx.Observable
import rx.subscriptions.Subscriptions

object RxBroadcastReceiver {

    fun create(context: Context,
               intentFilter: IntentFilter): Observable<Intent> {
        return Observable.create { subscriber ->
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    subscriber.onNext(intent)
                }
            }

            context.registerReceiver(receiver, intentFilter)

            subscriber.add(Subscriptions.create { context.unregisterReceiver(receiver) })
        }
    }
}
