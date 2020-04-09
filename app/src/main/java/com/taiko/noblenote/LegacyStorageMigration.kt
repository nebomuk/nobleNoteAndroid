package com.taiko.noblenote

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.taiko.noblenote.document.toSFile
import com.taiko.noblenote.preferences.PreferencesActivity
import java.io.File

/**
 * handles migration from earlier versions of this app that use absolute file paths instead of document content:// uris
 */
object LegacyStorageMigration {

    fun migrateFromLegacyStorage(activity: Activity)
    {
        if(Uri.parse(Pref.rootPath.value).scheme == null)
        {
            // these android versions do not use content:// uris in this app but instead rely on file:// uris
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
            {
                convertLegacyFilePathToUri() // this adds the file:// scheme to the path
            }
            else // Android N and greater, only use file:// uri for the fallback root path
            {
                val legacyFallbackRootPath = File(Pref.context.filesDir.absolutePath,"/nn").absolutePath;
                if(Pref.rootPath.value == legacyFallbackRootPath)
                {
                    convertLegacyFilePathToUri();  // this adds the file:// scheme to the path
                }
                else  // user interaction required, we want to migrate to content:// via the system file picker
                {
                    val legacyRootPath = Pref.rootPath.value;
                    // activate the fallback rootPath just in case the user cancels
                    // the system file picker and we would end up with an invalid rootPath
                    useFallbackRootPath()
                    showResolutionDialog(activity,legacyRootPath);
                }
            }
        }
    }

    private fun showResolutionDialog(activity : Activity, legacyRootPath : String): AlertDialog {
        val builder = AlertDialog.Builder(activity)

        builder.setTitle(R.string.title_upgrade_info);

        builder.setMessage(activity.getString(R.string.msg_upgrade, legacyRootPath));
        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
        }

        builder.setPositiveButton(android.R.string.ok) { dialog, id ->
            if(activity::class.java != PreferencesActivity::class.java) {
                val intent = Intent(activity, PreferencesActivity::class.java);
                intent.putExtra(PreferencesActivity.LAUNCH_SAF_FOLDER_PICKER,true);
                activity.startActivity(intent)
            }
        }

        return builder.show()
    }

    private fun useFallbackRootPath(){
        Pref.rootPath.onNext(Pref.fallbackRootPath);
    }

    private fun convertLegacyFilePathToUri() {
        Pref.rootPath.onNext(File(Pref.rootPath.value).toSFile().uri.toString());
    }
}