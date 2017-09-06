package com.taiko.noblenote

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_file_list.view.*






class FolderListFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_file_list, container, false)

        return rootView
    }

    private var mFolderListController : FolderListController? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        mFolderListController = FolderListController(this,view!!.recycler_view)


    }

    override fun onStart() {
        super.onStart()

        mFolderListController?.onStart();
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mFolderListController?.onDestroyView()
        mFolderListController = null;

    }






}
