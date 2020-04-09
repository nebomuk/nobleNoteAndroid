package com.taiko.noblenote

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import com.taiko.noblenote.preferences.PreferencesActivity
import com.taiko.noblenote.preferences.PreferencesActivity.Companion.LAUNCH_SAF_FOLDER_PICKER

object VolumeNotAccessibleDialog {

    fun create(activity: Activity): AlertDialog {
        val builder = AlertDialog.Builder(activity)


             builder.setTitle(R.string.title_storage_removed)

             builder.setMessage(R.string.msg_storage_removed)

        builder.setCancelable(false);

        builder.setPositiveButton(android.R.string.ok) { dialog, id ->
            if(activity::class.java != PreferencesActivity::class.java) {
                val intent = Intent(activity, PreferencesActivity::class.java);
                intent.putExtra(LAUNCH_SAF_FOLDER_PICKER, false);
                activity.startActivity(intent)
            }
        }

        return builder.create()
    }
}