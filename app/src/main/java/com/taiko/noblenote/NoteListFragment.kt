package com.taiko.noblenote

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class NoteListFragment : Fragment() {


    companion object {

        @JvmStatic
        val ARG_QUERY_TEXT = "query_text" // used to display results of a full text search

        @JvmStatic
        val ARG_FOLDER_PATH = "folder_path";

    }

    private var mNoteListController : NoteListController? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setHasOptionsMenu(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_file_list, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mNoteListController = NoteListController(this,view);

    }

    override fun onStart() {
        super.onStart()
        mNoteListController?.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mNoteListController?.onDestroyView();

    }




}
