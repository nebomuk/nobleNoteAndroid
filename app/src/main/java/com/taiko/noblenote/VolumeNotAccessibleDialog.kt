package com.taiko.noblenote

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.navigation.fragment.findNavController
import com.taiko.noblenote.filesystem.VolumeUtil
import com.taiko.noblenote.fragments.PreferenceFragment
import com.taiko.noblenote.util.loggerFor
import rx.Subscription

object VolumeNotAccessibleDialog {

    private val log = loggerFor()

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

    fun showAutomatically(fragment: Fragment): Subscription {
        return  VolumeUtil().volumeAccessible(fragment.requireContext(),Pref.rootPath.value)
                .filter { it == false }
                .subscribe {
                    log.i("volume $it not longer accessible, setting roothPath to internal storage");
                    Pref.rootPath.onNext(Pref.fallbackRootPath);
                    create(fragment).show();
                }
    }


}