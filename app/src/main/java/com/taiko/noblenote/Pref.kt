package com.taiko.noblenote

import com.chibatching.kotpref.KotprefModel
import rx.lang.kotlin.BehaviorSubject
import rx.subjects.BehaviorSubject
import java.io.File

/**
 * shared prefs
 */
object Pref : KotprefModel()
{
    val fallbackRootPath = File(context.filesDir.absolutePath,"/nn").absolutePath;

    // backing prefs
    private var mRootPath: String by stringPrefVar(default = fallbackRootPath) // the root path where the folders are stored
    private var mCurrentFolderPath: String by stringPrefVar(default = "") // the folder that has been isSelected in the ui

    // check if the app's stuff is saved on the internal storage
    var isInternalStorage = false
     get() = mRootPath.contains(context.filesDir.absolutePath)

    // reactive prefs
    val rootPath : BehaviorSubject<String> = BehaviorSubject(mRootPath);
    val currentFolderPath : BehaviorSubject<String> = BehaviorSubject(mCurrentFolderPath);

    init {
        // update backing prefs
        currentFolderPath.subscribe { mCurrentFolderPath = it }
        rootPath.subscribe { mRootPath = it }
    }

}