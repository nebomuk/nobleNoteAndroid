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


    val toolbarFindInFilesVisibility = MutableLiveData<Int>(View.GONE);

    val toolbarFindInFilesText = MutableLiveData<String>("");

    val fragmentFindInFilesVisible = MutableLiveData<Boolean>(false);

    val findInFilesResults = MutableLiveData<Collection<FindResult>>(emptyList());

    private val compositeSubscription = CompositeSubscription();

    init {
        toolbarFindInFilesText.observeForever {
            if(it.isNotBlank())
            {
                fragmentFindInFilesVisible.value = true;
            }
        }

        val inputText = toolbarFindInFilesText.toObservable();

        val queryTextObservable = inputText.filter { !it.isNullOrBlank() }
                //.throttleWithTimeout(400, TimeUnit.MILLISECONDS, Schedulers.io())
                .distinctUntilChanged();

        compositeSubscription += Pref.rootPath.map { path -> FindInFiles.findInFiles(SFile(path), queryTextObservable) }
                .switchOnNext()
                .subscribe {
                    findInFilesResults.postValue(it);
                }
    }


    fun onFabClick()
    {

    }

    fun onActionSearchClick() {
        toolbarFindInFilesVisibility.value = View.VISIBLE;
    }

    fun onClearTextClick()
    {
        toolbarFindInFilesText.value = "";
    }


    fun onToolbarFindInFilesBackClick()
    {
        toolbarFindInFilesVisibility.value = View.GONE;
        fragmentFindInFilesVisible.value = false;
        toolbarFindInFilesText.value = "";
    }

    override fun onCleared() {
        compositeSubscription.clear();
    }

}