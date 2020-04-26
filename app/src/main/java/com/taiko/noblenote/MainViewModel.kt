package com.taiko.noblenote

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.taiko.noblenote.document.SFile
import com.taiko.noblenote.extensions.toObservable
import rx.lang.kotlin.plusAssign
import rx.lang.kotlin.switchOnNext
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit

class MainViewModel(app : Application) : AndroidViewModel(app), LifecycleObserver {

    private val log = loggerFor()

    val showNewNoteDialog = SingleLiveEvent<Unit>()

    val showNewFolderDialog = SingleLiveEvent<Unit>()


    fun onFabFolderClick()
    {
        showNewFolderDialog.value = Unit;
    }

    fun onFabNoteClick()
    {
        showNewNoteDialog.value = Unit;
    }


}