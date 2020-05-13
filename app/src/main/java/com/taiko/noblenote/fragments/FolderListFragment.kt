package com.taiko.noblenote.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.taiko.noblenote.FolderListController
import com.taiko.noblenote.Pref
import com.taiko.noblenote.VolumeNotAccessibleDialog
import com.taiko.noblenote.filesystem.LegacyStorageMigration
import com.taiko.noblenote.databinding.FragmentFileListBinding
import com.taiko.noblenote.filesystem.VolumeUtil
import com.taiko.noblenote.util.loggerFor


class FolderListFragment : Fragment() {

    private val log = loggerFor();

    private lateinit var binding: FragmentFileListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentFileListBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner;
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LegacyStorageMigration.showResolutionDialogIfRequired(this);

        val folderListController = FolderListController(this, binding)
        viewLifecycleOwner.lifecycle.addObserver(folderListController);

    }

}
