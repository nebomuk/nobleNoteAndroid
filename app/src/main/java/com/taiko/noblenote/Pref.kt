package com.taiko.noblenote

import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.chibatching.kotpref.KotprefModel
import com.commonsware.cwac.document.DocumentFileCompat
import rx.lang.kotlin.BehaviorSubject
import rx.subjects.BehaviorSubject
import java.io.File

/**
 * shared prefs
 */
object Pref : KotprefModel()
{
    // constants
    // TODO check external storage package
    val fallbackRootPath = File(context.filesDir.absolutePath,"/nn").toSFile().uri.toString()

    // backing prefs
    private var mRootPath: String by stringPrefVar(default = fallbackRootPath) // the root path where the folders are stored
    private var mCurrentFolderPath: String by stringPrefVar(default = "") // the folder that has been isSelected in the ui

    // check if the app's stuff is saved on the internal storage
    var isInternalStorage = false
     get() = mRootPath.contains(context.filesDir.absolutePath)

    // reactive prefs
    val rootPath : BehaviorSubject<String> = BehaviorSubject(mRootPath);
    val currentFolderPath : BehaviorSubject<String> = BehaviorSubject(mCurrentFolderPath);

    // other prefs
    var isAutoSaveEnabled : Boolean by booleanPrefVar(default = true) // file autosave onStop

    init {
        // update backing prefs
        currentFolderPath.subscribe { mCurrentFolderPath = it }
        rootPath.subscribe { mRootPath = it }
    }

}