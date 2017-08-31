package com.taiko.noblenote

import android.os.Environment
import com.chibatching.kotpref.KotprefModel
import rx.lang.kotlin.BehaviorSubject
import rx.subjects.BehaviorSubject

/**
 * shared prefs
 */
object Pref : KotprefModel()
{
    private val fallbackRootPath = Environment.getExternalStorageDirectory().absolutePath + "/nn";

    // backing prefs
    private var mRootPath: String by stringPrefVar(default = fallbackRootPath) // the root path where the folders are stored
    private var mCurrentFolderPath: String by stringPrefVar(default = "") // the folder that has been isSelected in the ui

    // reactive prefs
    val rootPath : BehaviorSubject<String> = BehaviorSubject(mRootPath);
    val currentFolderPath : BehaviorSubject<String> = BehaviorSubject(mCurrentFolderPath);

    init {
        // update backing prefs
        currentFolderPath.subscribe { mCurrentFolderPath = it }
        rootPath.subscribe { mRootPath = it }
    }

}