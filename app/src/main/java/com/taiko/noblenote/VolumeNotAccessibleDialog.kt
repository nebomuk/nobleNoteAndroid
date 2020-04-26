package com.taiko.noblenote

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taiko.noblenote.preferences.PreferenceFragment

object VolumeNotAccessibleDialog {

    fun create(fragment: Fragment): AlertDialog {
        val builder = AlertDialog.Builder(fragment.requireActivity())


             builder.setTitle(R.string.title_storage_removed)

             builder.setMessage(R.string.msg_storage_removed)

        builder.setCancelable(false);

        builder.setPositiveButton(android.R.string.ok) { dialog, id ->
            val bundle = Bundle();
            bundle.putString(PreferenceFragment.LAUNCH_SAF_FOLDER_PICKER, PreferenceFragment.LAUNCH_SAF_FOLDER_PICKER);
            fragment.findNavController().navigate(R.id.preferenceFragment,bundle)
        }

        return builder.create()
    }
}