package com.taiko.noblenote

import android.os.Environment
import com.chibatching.kotpref.KotprefModel
import rx.Observable
import rx.lang.kotlin.PublishSubject
import rx.subjects.PublishSubject


object Pref : KotprefModel()
{
    private val fallbackRootPath = Environment.getExternalStorageDirectory().absolutePath + "/nn";

    // backing field
    private var mRootPath: String by stringPrefVar(default = fallbackRootPath) // the root path where the folders are stored
    var rootPath : String

    get() = mRootPath
    set(value) {
        mRootPath = value
        rootPathChangedSubject.onNext(value);
    }

    private val rootPathChangedSubject : PublishSubject<String> = PublishSubject();

    fun rootPathChanged() : Observable<String>
    {
        return rootPathChangedSubject.startWith(rootPath);
    }
    var selectedFolderPath: String by stringPrefVar(default = "") // the folder that has been isSelected in the ui
}