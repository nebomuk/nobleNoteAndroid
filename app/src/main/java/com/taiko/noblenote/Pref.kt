package com.taiko.noblenote

import android.os.Environment
import com.chibatching.kotpref.KotprefModel

/**
 * Created by Taiko on 06.04.2016.
 */
object Pref : KotprefModel()
{
    private val fallbackRootPath = Environment.getExternalStorageDirectory().absolutePath + "/nn";
    var rootPath: String by stringPrefVar(default = fallbackRootPath) // the root path where the folders are stored
    var selectedFolderPath: String by stringPrefVar(default = "") // the folder that has been selected in the ui
}