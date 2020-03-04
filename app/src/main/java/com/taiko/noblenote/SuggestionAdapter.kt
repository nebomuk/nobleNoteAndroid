package com.taiko.noblenote

import android.content.Context
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * displays suggestions in an input field by using full text search inside files and dirs in the app pref's root path with the given char sequence
 */
class SuggestionAdapter constructor(context: Context, queryTextObservable: Observable<CharSequence>) : ArrayAdapter<SFile>(context,android.R.layout.simple_dropdown_item_1line)
{
    val TAG : String = SuggestionAdapter::class.java.simpleName

    init {
        setNotifyOnChange(false)

        FindInFiles.findInFiles(SFile(Pref.rootPath.value),queryTextObservable)

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.index() == 0L)
                   {
                       clear();
                   }
                    add(it.value());
                    this.sort { file, file2 -> file.name.compareTo(file2.name) }
                    this.notifyDataSetChanged();
                }
    }







}



