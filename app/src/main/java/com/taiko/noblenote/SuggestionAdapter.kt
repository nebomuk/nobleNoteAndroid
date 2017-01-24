package com.taiko.noblenote

import android.content.Context
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File

/**
 * displays suggestions in an input field by using full text search inside files and dirs in the app pref's root path with the given char sequence
 */
class SuggestionAdapter constructor(context: Context, queryTextObservable: Observable<CharSequence>) : ArrayAdapter<File>(context,android.R.layout.simple_dropdown_item_1line)
{
    val TAG : String = SuggestionAdapter::class.java.simpleName

    init {
        setNotifyOnChange(false)


        queryTextObservable
                .switchMap {

                    val queryText = it;
                    FindInFiles.findHtmlInFiles(Pref.rootPath.value, queryText)
                            .compose(MapWithIndex.instance())
                            .subscribeOn(Schedulers.io())
                            .concatWith(Observable.never()) // never complete
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.index() == 0L)
                   {
                       clear();
                   }
                    add(File(it.value() as String));
                    this.sort { file, file2 -> file.name.compareTo(file2.name) }
                    this.notifyDataSetChanged();
                }
    }







}



