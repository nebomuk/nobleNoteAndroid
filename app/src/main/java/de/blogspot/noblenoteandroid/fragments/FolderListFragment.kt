package de.blogspot.noblenoteandroid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.blogspot.noblenoteandroid.FolderListController
import de.blogspot.noblenoteandroid.filesystem.LegacyStorageMigration
import de.blogspot.noblenoteandroid.databinding.FragmentFileListBinding
import de.blogspot.noblenoteandroid.util.loggerFor


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
