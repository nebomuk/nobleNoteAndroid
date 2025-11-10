package com.blogspot.noblenoteandroid

import android.net.Uri
import android.os.Build
import android.os.Environment
import com.chibatching.kotpref.KotprefModel
import com.blogspot.noblenoteandroid.filesystem.SFile
import com.blogspot.noblenoteandroid.filesystem.toSFile
import rx.lang.kotlin.BehaviorSubject
import rx.subjects.BehaviorSubject
import java.io.File

/**
 * shared prefs
 */
object Pref : KotprefModel()
{
    // constants
    val fallbackRootPath = File(context.filesDir.absolutePath,"/nn").toSFile().uri.toString()

    // backing prefs
    private var mRootPath: String by stringPrefVar(default = fallbackRootPath) // the root path where the folders are stored
    private var mCurrentFolderPath: String by stringPrefVar(default = fallbackRootPath) // the folder that has been isSelected in the ui



    // reactive prefs
    val rootPath : BehaviorSubject<String> = BehaviorSubject(mRootPath);
    val currentFolderPath : BehaviorSubject<String> = BehaviorSubject(mCurrentFolderPath);


    val isExternalOrSafStorage : Boolean get() {

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
        {
            return Uri.parse(Pref.rootPath.value).scheme == "content";
        }
        else
        {
            return Uri.parse(Pref.rootPath.value).path?.contains(Environment.getExternalStorageDirectory().absolutePath) ?: false
        }
    }

    // other prefs
    var isAutoSaveEnabled : Boolean by booleanPrefVar(default = false) // file autosave onStop, keep default in sync with pref fragment!

    init {
        // update backing prefs
        currentFolderPath.subscribe { mCurrentFolderPath = it }
        rootPath.subscribe {
            SFile.invalidateAllFileListCaches();
            mRootPath = it
        }
    }

}