package com.taiko.noblenote

import com.taiko.noblenote.document.SFile
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.switchOnNext

/**
 * displays suggestions in an input field by using full text search inside files and dirs in the app pref's root path with the given char sequence
 */
object SearchSuggestions

{
    fun apply(adapter: ArrayAdapter<SFile>, queryTextObservable: Observable<CharSequence>, pathToSearch: Observable<SFile>): Subscription {
        adapter.setNotifyOnChange(false)
        return pathToSearch.map { path -> FindInFiles.findInFiles(path, queryTextObservable) }
                .switchOnNext()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.index() == 0L) {
                        adapter.clear();
                    }
                    adapter.add(it.value());
                    adapter.sort { file, file2 -> file.name.compareTo(file2.name) }
                    adapter.notifyDataSetChanged();
                }
    }
}



