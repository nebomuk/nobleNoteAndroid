package com.taiko.noblenote.findinfiles

import android.app.Application
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.taiko.noblenote.Pref
import com.taiko.noblenote.R
import com.taiko.noblenote.SingleLiveEvent
import com.taiko.noblenote.document.SFile
import com.taiko.noblenote.extensions.toObservable
import com.taiko.noblenote.loggerFor
import rx.lang.kotlin.plusAssign
import rx.lang.kotlin.switchOnNext
import rx.subscriptions.CompositeSubscription

class FindInFilesViewModel(app : Application) : AndroidViewModel(app), LifecycleObserver {

    private val log = loggerFor();

    val toolbarFindInFilesText = MutableLiveData<String>("");

    val findInFilesResults = MutableLiveData<List<FindResult>>(emptyList());

    val backgroundTransparent = MutableLiveData<Boolean>(true);

    val startNoteEditor = SingleLiveEvent<SFile>()


    private val compositeSubscription = CompositeSubscription();

    init {
        toolbarFindInFilesText.observeForever {
            backgroundTransparent.value = it.isBlank()
        }

        val inputText = toolbarFindInFilesText.toObservable();

        val queryTextObservable = inputText.filter { it != null }
                //.throttleWithTimeout(400, TimeUnit.MILLISECONDS, Schedulers.io())
                .distinctUntilChanged();

        compositeSubscription += Pref.rootPath.map { path -> FindInFilesEngine.findInFiles(SFile(path), queryTextObservable) }
                .switchOnNext()
                .subscribe {
                    findInFilesResults.postValue(it);
                }
    }



    fun onClearTextClick()
    {
        toolbarFindInFilesText.value = "";
    }

    fun onFindItemClick(file : SFile)
    {
        log.v("find result clicked:" + file.name);
        startNoteEditor.value = file;
    }
    override fun onCleared() {
        compositeSubscription.clear();
    }

    fun onToolbarFindInFilesBackClick()
    {
        // TODO pop fragment
    }



}