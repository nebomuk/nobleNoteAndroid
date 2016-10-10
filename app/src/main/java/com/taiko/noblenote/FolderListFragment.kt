package com.taiko.noblenote

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import kotlinx.android.synthetic.main.fragment_file_list.view.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.io.FileFilter


class FolderListFragment : Fragment() {

    private val mActivatedPosition = ListView.INVALID_POSITION
    private val mTwoPane = false
    private val mCompositeSubscription = CompositeSubscription()


    private lateinit var fileSystemAdapter: RecyclerFileAdapter


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
        rv.layoutManager = LinearLayoutManager(activity)



        val app = (activity.application as MainApplication)
        mCompositeSubscription += rv.itemClicks()
                .doOnNext { Log.d("","item pos clicked: " + it) }
                .subscribe { app.uiCommunicator.folderSelected.onNext(fileSystemAdapter.getItem(it)) }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCompositeSubscription.clear()

    }




}
