package com.taiko.noblenote

import android.content.Context
import android.util.Log
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.switchOnNext
import rx.lang.kotlin.toObservable
import rx.lang.kotlin.toSingletonObservable
import java.io.File

/**
 * Created by fabdeuch on 04.09.2016.
 */
class SuggestionAdapter constructor(context: Context, queryTextObservable: Observable<CharSequence>) : ArrayAdapter<File>(context,android.R.layout.simple_dropdown_item_1line)
{
    val TAG : String = SuggestionAdapter::class.java.simpleName

    init {
        setNotifyOnChange(true)

        queryTextObservable
                .map {
                    val queryText = it;
                    Log.v(TAG,"clear")
                    findHtmlInFiles(Pref.rootPath, queryText).map { File(it) }.toSortedList()
                }
                .switchOnNext()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
//            Timber.v(it)
                    clear() // clear adapter as side effect
                    addAll(it);
                }
    }



    fun  findHtmlInFiles(path : String, queryText: CharSequence) : Observable<String> =
            File(path).walkBottomUp().filter { it.isFile && !it.isHidden }
                    .toObservable()
                    .map {
                        val filePath = it.path;
                        //                Timber.i(it.name)
                        if (it.name.contains(queryText, true)) {
                            filePath.toSingletonObservable() // file name contains the queryText, return the filePath
                        } else {
                            Observable.using(// open the file and try to find the queryText inside
                                    { it.bufferedReader() },
                                    { it.lineSequence().toObservable() },
                                    { it.close() })
                                    .exists { it.contains(queryText, true) }
                                    .filter { it == true } // found
                                    .map { filePath }
                        }
                    }.concat()

    fun <T> Observable<Observable<T>>.concat(): Observable<T> = Observable.concat(this)
}



