package com.taiko.noblenote.preferences

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.taiko.noblenote.Pref
import com.taiko.noblenote.R
import com.taiko.noblenote.document.SFile
import com.taiko.noblenote.document.TreeUriUtil
import com.taiko.noblenote.document.toSFile
import kotlinx.android.synthetic.main.preferences_activity.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import rx_activity_result.RxActivityResult
import java.io.File


class PreferenceFragment : PreferenceFragmentCompat() {

    private val mCompositeSubscription = CompositeSubscription();

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        val manager: PreferenceManager = preferenceManager
        manager.sharedPreferencesName = Pref.javaClass.simpleName;

        setPreferencesFromResource(R.xml.root_preferences, rootKey)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCompositeSubscription += Pref.rootPath.map { Pref.isExternalOrSafStorage }.subscribe {
            findPreference<Preference>(getString(R.string.pref_key_internal_storage))?.isEnabled = it;
        }

        findPreference<Preference>(getString(R.string.pref_key_internal_storage))?.setOnPreferenceClickListener {

            if(SFile(Pref.rootPath.value).listFiles().count() > 0)
            {
                showWarningBeforeInternalStorage { useInternalStorage(); }
            }
            else
            {
                useInternalStorage();
            }
            true;
        }

        findPreference<Preference>(getString(R.string.pref_key_saf_picker))?.setOnPreferenceClickListener {

            if(SFile(Pref.rootPath.value).listFiles().count() > 0)
            {
                showWarningBeforeSaf {startSafFolderPicker(activity!!)}
            }
            else
            {
                startSafFolderPicker(activity!!)
            }
            true;
        }

    }

    private fun useInternalStorage() {
        Pref.rootPath.onNext(Pref.fallbackRootPath)
        Snackbar.make(activity!!.linear_layout, R.string.msg_store_internal_storage, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mCompositeSubscription.clear();
    }

    private fun showWarningBeforeSaf(onPositive : () -> Unit)
    {
        val builder = AlertDialog.Builder(activity)

        builder.setTitle(R.string.title_use_external_storage)

        if(Pref.isExternalOrSafStorage)
        {
            builder.setMessage(R.string.msg_existing_notebooks_not_transferred)

        }
        else
        {
            builder.setMessage(getString(R.string.msg_existing_notebooks_not_transferred) + "\n"
            + getString(R.string.msg_store_internal_storage_again))
        }

        builder.setPositiveButton(android.R.string.ok) { dialog, id ->
            onPositive();
        }
        builder.setNegativeButton(android.R.string.cancel,null);
        builder.show()
    }

    private fun showWarningBeforeInternalStorage(onPositive : () -> Unit)
    {
        val builder = AlertDialog.Builder(activity)

        builder.setTitle(R.string.title_store_internal_storage)

        builder.setMessage(getString(R.string.msg_existing_notebooks_not_transferred))

        builder.setPositiveButton(android.R.string.ok) { dialog, id ->
            onPositive();
        }
        builder.setNegativeButton(android.R.string.cancel,null);

        builder.show()
    }

    private fun startSafFolderPicker(activity: Activity) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) // google play version min sdk is 24, sdk 21-23 is only for internal use
        {
            Snackbar.make(activity.linear_layout, "Android 6 Marshmallow does not support writing notebooks to the external storage",
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        val filePickerDialogIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .apply {
                    putExtra("android.content.extra.SHOW_ADVANCED", true);
                    putExtra("android.content.extra.FANCY", true);
                    putExtra("android.content.extra.SHOW_FILESIZE", true);
                }

        RxActivityResult.on(activity).startIntent(filePickerDialogIntent).subscribe {
            if ((it.resultCode() == Activity.RESULT_OK)) {
                val uri: Uri? = it.data().data

                val takeFlags = it.data().flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                //noinspection WrongConstant
                activity.contentResolver.takePersistableUriPermission(uri!!, takeFlags)

                if (uri != Uri.parse(Pref.rootPath.toString())) {
                    SFile.clearGlobalDocumentCache();


                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)  // dont forget to grant runtime permission when testing this on newer devices
                    {
                        // check if selected uri is 3rd party document provider which does not work,
                        // for example content://com.pleco.chinesesystem.localstorage.documents
                        val isExternalStorageDocument = "com.android.externalstorage.documents" == uri.authority;

                        if (!isExternalStorageDocument) {
                            Snackbar.make(activity.linear_layout, "This document cannot be used. Please select the default external storage when there are multiple document providers",
                                    Snackbar.LENGTH_LONG).show();
                            return@subscribe;
                        }

                        // use java.io.File API wrapped in SFile because SAF limitations for Android 5
                        val path = TreeUriUtil.treeUriToFilePath(uri, activity);
                        Pref.rootPath.onNext(File(path).toSFile().uri.toString());
                    } else {
                        Pref.rootPath.onNext(SFile(uri).uri.toString());
                    }
                }
                val selected = SFile(Pref.rootPath.value).uri.lastPathSegment?.split(":")?.lastOrNull()
                if(selected != null)
                {
                    Snackbar.make(activity.linear_layout, getString(R.string.msg_directory_selected) + " " + selected, Snackbar.LENGTH_LONG).show();
                }

            }
        }

    }



}