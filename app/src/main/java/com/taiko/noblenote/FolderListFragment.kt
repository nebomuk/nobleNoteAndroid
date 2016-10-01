package com.taiko.noblenote

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import kotlinx.android.synthetic.main.fragment_file_list.view.*
import java.io.File
import java.io.FileFilter


class FolderListFragment : Fragment() {

    private val mActivatedPosition = ListView.INVALID_POSITION
    private val mTwoPane = false

    interface Callbacks {

        fun onItemSelected(id: String)
    }

    private var fileSystemAdapter: RecyclerFileAdapter? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_file_list, container, false)

        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        val rv = view?.recycler_view as RecyclerView

        val dir = File(Pref.rootPath)
        if (!dir.exists())
            dir.mkdirs()

        // the following code lists only visible folders and push their names into an ArrayAdapter
        val folderFilter = FileFilter { pathname -> pathname.isDirectory && !pathname.isHidden }

        fileSystemAdapter = RecyclerFileAdapter(File(Pref.rootPath), folderFilter)
        rv.adapter = fileSystemAdapter

    }




}
