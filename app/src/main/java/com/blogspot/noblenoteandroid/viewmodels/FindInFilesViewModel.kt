package com.blogspot.noblenoteandroid.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.blogspot.noblenoteandroid.Pref
import com.blogspot.noblenoteandroid.util.SingleLiveEvent
import com.blogspot.noblenoteandroid.filesystem.SFile
import com.blogspot.noblenoteandroid.extensions.toObservable
import com.blogspot.noblenoteandroid.filesystem.FindInFilesEngine
import com.blogspot.noblenoteandroid.filesystem.FindResult
import com.blogspot.noblenoteandroid.util.loggerFor
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