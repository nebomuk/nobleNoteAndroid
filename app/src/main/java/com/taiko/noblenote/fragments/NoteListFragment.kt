package com.taiko.noblenote.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.taiko.noblenote.NoteListController
import com.taiko.noblenote.R
import com.taiko.noblenote.VolumeNotAccessibleDialog
import com.taiko.noblenote.databinding.FragmentFileListBinding


class NoteListFragment : Fragment() {


    private lateinit var binding: FragmentFileListBinding

    companion object {

        @JvmStatic
        val ARG_FOLDER_PATH = "folder_path";

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setHasOptionsMenu(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentFileListBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner;
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val noteListController = NoteListController(this, binding);
        viewLifecycleOwner.lifecycle.addObserver(noteListController);

    }


}
