package com.blogspot.noblenoteandroid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blogspot.noblenoteandroid.NoteListController
import com.blogspot.noblenoteandroid.databinding.FragmentFileListBinding


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
