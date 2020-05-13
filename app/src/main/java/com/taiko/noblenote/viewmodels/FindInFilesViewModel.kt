package com.taiko.noblenote.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.taiko.noblenote.Pref
import com.taiko.noblenote.util.SingleLiveEvent
import com.taiko.noblenote.filesystem.SFile
import com.taiko.noblenote.extensions.toObservable
import com.taiko.noblenote.filesystem.FindInFilesEngine
import com.taiko.noblenote.filesystem.FindResult
import com.taiko.noblenote.util.loggerFor
import rx.lang.kotlin.plusAssign
import rx.lang.kotlin.switchOnNext
import rx.subscriptions.CompositeSubscription

class FindInFilesViewModel(app : Application) : AndroidViewModel(app), LifecycleObserver {

    private val log = loggerFor();

    val toolbarFindInFilesText = MutableLiveData<String>("");

    val findInFilesResults = MutableLiveData<List<FindResult>>(emptyList());

    val nothingFound = MutableLiveData<Boolean>(false);

    val queryTextBlank = MutableLiveData<Boolean>(true);

    val startNoteEditor = SingleLiveEvent<FindResult>()


    private val compositeSubscription = CompositeSubscription();

    init {
        toolbarFindInFilesText.observeForever {
            queryTextBlank.value = it.isBlank()
        }

        val inputText = toolbarFindInFilesText.toObservable();

        val queryTextObservable = inputText.filter { it != null }
                .distinctUntilChanged();

        val findResults = Pref.rootPath
                .map { path -> FindInFilesEngine.findInFiles(SFile(path), queryTextObservable) }
                .switchOnNext()
                .share();

        compositeSubscription += findResults.subscribe {
            nothingFound.postValue(it.nothingFound)
        }

        compositeSubscription +=findResults.map { it.list }
                .subscribe {
                    findInFilesResults.postValue(it);
                }
    }

    fun onClearTextClick()
    {
        toolbarFindInFilesText.value = "";
    }

    fun onFindItemClick(findResult: FindResult)
    {
        log.v("find result clicked:" + findResult.file.name);
        startNoteEditor.value = findResult;
    }
    override fun onCleared() {
        compositeSubscription.clear();
    }


}