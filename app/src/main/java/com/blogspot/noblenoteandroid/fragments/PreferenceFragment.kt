package com.blogspot.noblenoteandroid.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.blogspot.noblenoteandroid.Pref
import com.blogspot.noblenoteandroid.R
import com.blogspot.noblenoteandroid.VolumeNotAccessibleDialog
import com.blogspot.noblenoteandroid.filesystem.SFile
import com.blogspot.noblenoteandroid.filesystem.TreeUriUtil
import com.blogspot.noblenoteandroid.filesystem.VolumeUtil
import com.blogspot.noblenoteandroid.filesystem.toSFile
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import rx_activity_result.RxActivityResult
import java.io.File


class PreferenceFragment : PreferenceFragmentCompat() {

    private val mCompositeSubscription = CompositeSubscription();

    companion object
    {
        const val LAUNCH_SAF_FOLDER_PICKER = "show_saf_folder_picker"
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        val manager: PreferenceManager = preferenceManager
        manager.sharedPreferencesName = Pref.javaClass.simpleName;

        setPreferencesFromResource(R.xml.root_preferences, rootKey)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbarView = requireView().findViewById<Toolbar>(R.id.toolbar)

        toolbarView.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbarView.setNavigationOnClickListener { findNavController().popBackStack() }
        toolbarView.setTitle(R.string.title_activity_preferences)

        mCompositeSubscription += Pref.rootPath.map { Pref.isExternalOrSafStorage }.subscribe {
            findPreference<Preference>(getString(R.string.pref_key_internal_storage))?.isEnabled = it;
        }

        findPreference<Preference>(getString(R.string.pref_key_internal_storage))?.setOnPreferenceClickListener {

            if(VolumeUtil.fileOrContentUriAccessible(requireActivity(),Pref.rootPath.value) && SFile(Pref.rootPath.value).listFiles().count() > 0)
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

            if(VolumeUtil.fileOrContentUriAccessible(requireActivity(),Pref.rootPath.value) && SFile(Pref.rootPath.value).listFiles().count() > 0)
            {
                showWarningBeforeSaf {startSafFolderPicker(requireActivity())}
            }
            else
            {
                startSafFolderPicker(requireActivity())
            }
            true;
        }

        val showFolderPicker = activity?.intent?.extras?.getBoolean(LAUNCH_SAF_FOLDER_PICKER);
        if(showFolderPicker == true)
        {
            startSafFolderPicker(requireActivity())
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }

        if(arguments?.getString(LAUNCH_SAF_FOLDER_PICKER,null) != null)
        {
            startSafFolderPicker(requireActivity());
        }

    }

    private fun useInternalStorage() {
        Pref.rootPath.onNext(Pref.fallbackRootPath)
        Snackbar.make(requireView(), R.string.msg_store_internal_storage, Snackbar.LENGTH_LONG).show()
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
            Snackbar.make(requireView(), "Android 6 Marshmallow does not support writing notebooks to the external storage",
                    Snackbar.LENGTH_LONG).show();
            return;
        }



        val filePickerDialogIntent: Intent
        filePickerDialogIntent = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val storageManager = requireContext().getSystemService(Context.STORAGE_SERVICE) as StorageManager;
            storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
        } else {
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        }


        filePickerDialogIntent
                .apply {
                    putExtra("android.content.extra.SHOW_ADVANCED", true);
                    putExtra("android.content.extra.FANCY", true);
                    putExtra("android.content.extra.SHOW_FILESIZE", true);
                }

        RxActivityResult.on(activity).startIntent(filePickerDialogIntent).subscribe {
            if ((it.resultCode() == Activity.RESULT_OK)) {
                val uri: Uri? = it.data().data

                val takeFlags = it.data().flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)


                // check if selected uri is 3rd party document provider which does not work,
                // for example content://com.pleco.chinesesystem.localstorage.documents

                val downloadsDocumentProviderAuthority =  "com.android.providers.downloads.documents"
                val externalStorageAuthority = "com.android.externalstorage.documents"

                val isDownloadsFolder = externalStorageAuthority == uri?.authority;


                if (!isDownloadsFolder) {
                    Snackbar.make(requireView(), R.string.msg_saf_downloads_not_supported,
                            Snackbar.LENGTH_LONG).show();
                    return@subscribe;
                }


                val isExternalStorageDocument = externalStorageAuthority == uri?.authority;

                if (!isExternalStorageDocument) {
                    Snackbar.make(requireView(), R.string.msg_saf_provider_not_supported,
                            Snackbar.LENGTH_LONG).show();
                    return@subscribe;
                }

                //noinspection WrongConstant
                activity.contentResolver.takePersistableUriPermission(uri!!, takeFlags)

                if (uri != Uri.parse(Pref.rootPath.toString())) {
                    SFile.clearGlobalDocumentCache();


                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)  // dont forget to grant runtime permission when testing this on newer devices
                    {

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
                    Snackbar.make(requireView(), getString(R.string.msg_directory_selected) + " " + selected, Snackbar.LENGTH_LONG).show();
                }

            }
        }

    }

    private fun onBackPressed() {
        val dlg = VolumeNotAccessibleDialog.create(this@PreferenceFragment);
        if (!VolumeUtil.fileOrContentUriAccessible(requireContext(), Pref.rootPath.value)) {
            dlg.show();
        } else {
            findNavController().navigateUp();
        }
    }



}