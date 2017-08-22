package com.taiko.noblenote

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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


    private lateinit var recyclerFileAdapter: RecyclerFileAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_file_list, container, false)

        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        val rv = view?.recycler_view as RecyclerView
        rv.itemAnimator = DefaultItemAnimator();

        val dir = File(Pref.rootPath.value)
        if (!dir.exists())
            dir.mkdirs()

        // the following code lists only visible folders and push their names into an ArrayAdapter
        val folderFilter = FileFilter { pathname -> pathname.isDirectory && !pathname.isHidden }

        recyclerFileAdapter = RecyclerFileAdapter()
        recyclerFileAdapter.filter = folderFilter
        mCompositeSubscription += Pref.rootPath.skip(1).subscribe {
            recyclerFileAdapter.path = File(it)
            recyclerFileAdapter.refresh(activity)
        }

        rv.adapter = recyclerFileAdapter
        rv.layoutManager = LinearLayoutManager(activity)

        val listController = ListController(activity as MainActivity,rv)

        val app = (activity.application as MainApplication)
        mCompositeSubscription += listController.itemClicks()
                .doOnNext { KLog.d("item pos clicked: " + it) }
                .subscribe { app.uiCommunicator.folderSelected.onNext(recyclerFileAdapter.getItem(it)) }



        mCompositeSubscription += app.uiCommunicator.createFolderClick.subscribe { recyclerFileAdapter.addFile(it) }

        mCompositeSubscription += app.uiCommunicator.swipeRefresh.subscribe { recyclerFileAdapter.refresh(activity) }

    }

    override fun onStart() {
        super.onStart()
        recyclerFileAdapter.refresh(activity);
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCompositeSubscription.clear()

    }




}
