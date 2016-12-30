package com.taiko.noblenote

import android.os.Environment
import com.chibatching.kotpref.KotprefModel
import rx.lang.kotlin.BehaviorSubject
import rx.subjects.BehaviorSubject


object Pref : KotprefModel()
{
    private val fallbackRootPath = Environment.getExternalStorageDirectory().absolutePath + "/nn";

    // backing pref
    private var mRootPath: String by stringPrefVar(default = fallbackRootPath) // the root path where the folders are stored

    val rootPath : BehaviorSubject<String> = BehaviorSubject(mRootPath);

    // backing pref
    private var mCurrentFolderPath: String by stringPrefVar(default = "") // the folder that has been isSelected in the ui

    val currentFolderPath : BehaviorSubject<String> = BehaviorSubject(mCurrentFolderPath);

    init {
        // update backing prefs
        currentFolderPath.subscribe { mCurrentFolderPath = it }
        rootPath.subscribe { mRootPath = it }
    }

}