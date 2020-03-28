package com.taiko.noblenote

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import com.taiko.noblenote.document.VolumeUtil
import com.taiko.noblenote.preferences.PreferencesActivity
import rx.Subscription
import java.util.*

object VolumeNotAccessibleDialog {

     fun create(activity: Activity): AlertDialog {
        val builder = AlertDialog.Builder(activity)

        builder.setTitle("Storage device not available")

        builder.setMessage("The storage location is not longer available. ")

        builder.setPositiveButton("Select another storage location") { dialog, id ->
            if(activity::class.java != PreferencesActivity::class.java)
            activity.startActivity(Intent(activity,PreferencesActivity::class.java))
        }

        builder.setCancelable(false);

        return builder.create()
    }
}