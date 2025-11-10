package com.blogspot.noblenoteandroid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blogspot.noblenoteandroid.FolderListController
import com.blogspot.noblenoteandroid.filesystem.LegacyStorageMigration
import com.blogspot.noblenoteandroid.databinding.FragmentFileListBinding
import com.blogspot.noblenoteandroid.util.loggerFor


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
