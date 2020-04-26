package com.taiko.noblenote

import android.app.AlertDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taiko.noblenote.document.toSFile
import com.taiko.noblenote.preferences.PreferenceFragment
import java.io.File

/**
 * handles migration from earlier versions of this app that use absolute file paths instead of document content:// uris
 */
object LegacyStorageMigration {

    fun migrateFromLegacyStorage(fragment: Fragment)
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
                    showResolutionDialog(fragment,legacyRootPath);
                }
            }
        }
    }

    private fun showResolutionDialog(fragment: Fragment, legacyRootPath : String): AlertDialog {
        val builder = AlertDialog.Builder(fragment.requireActivity())

        builder.setTitle(R.string.title_upgrade_info);

        builder.setMessage(fragment.requireActivity().getString(R.string.msg_upgrade, legacyRootPath));
        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
        }

        builder.setPositiveButton(android.R.string.ok) { dialog, id ->
            val bundle = Bundle();
            bundle.putString(PreferenceFragment.LAUNCH_SAF_FOLDER_PICKER,PreferenceFragment.LAUNCH_SAF_FOLDER_PICKER);
            fragment.findNavController().navigate(R.id.preferenceFragment,bundle)
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