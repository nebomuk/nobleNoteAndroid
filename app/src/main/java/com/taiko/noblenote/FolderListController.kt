package com.taiko.noblenote

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.io.FileFilter

/**
 * Created by taiko
 */
class FolderListController(private var fragment: Fragment, recyclerView: RecyclerView) {

    private var mTwoPane = false
    private val mCompositeSubscription = CompositeSubscription()


    private  var recyclerFileAdapter: RecyclerFileAdapter

    init {
        mTwoPane = (fragment.activity as MainActivity).twoPane

        recyclerView.itemAnimator = DefaultItemAnimator();

        val dir = File(Pref.rootPath.value)
        if (!dir.exists())
            dir.mkdirs()

        // the following code lists only visible folders and push their names into an ArrayAdapter
        val folderFilter = FileFilter { pathname -> pathname.isDirectory && !pathname.isHidden }

        recyclerFileAdapter = RecyclerFileAdapter()
        recyclerFileAdapter.filter = folderFilter

        recyclerView.adapter = recyclerFileAdapter
        recyclerView.layoutManager = LinearLayoutManager(fragment.activity)

        val listController = ListSelectionController(fragment.activity as MainActivity, recyclerView)
        listController.isTwoPane = mTwoPane;

        val app = (fragment.activity.application as MainApplication)

        if(mTwoPane)
        {
            recyclerFileAdapter.selectFolderOnClick =true
            mCompositeSubscription += recyclerFileAdapter.selectedFolder().subscribe {
                if(it == RecyclerView.NO_POSITION)
                {
                    val noteFragment = fragment.fragmentManager.findFragmentById(R.id.item_detail_container);
                    if(noteFragment != null)
                    {
                        fragment.fragmentManager.beginTransaction().remove(noteFragment).commit();
                    }
                }
                else
                {
                    val item = recyclerFileAdapter.getItem(it)
                    if(item != null) {
                        Pref.currentFolderPath.onNext(item.absolutePath)
                        showNoteFragment(item.absolutePath)
                    }

                }
            }

        }
        else {
            mCompositeSubscription += listController.itemClicks()
                    .subscribe {
                        val item = recyclerFileAdapter.getItem(it);
                        if (item != null) {
                            Pref.currentFolderPath.onNext(item.absolutePath)
                            showNoteFragment(item.absolutePath)
                        }

                    }
        }


        mCompositeSubscription += app.eventBus.createFolderClick.subscribe { recyclerFileAdapter.addFile(it) }

        mCompositeSubscription += app.eventBus.swipeRefresh.subscribe {
            if (fragment != null) {
                recyclerFileAdapter.refresh(fragment.activity)
            }
        };
    }

    fun showNoteFragment(folderPath: String) {

        val noteFragment = NoteListFragment()
        val arguments = Bundle()
        arguments.putString(NoteListFragment.ARG_FOLDER_PATH, folderPath)
        noteFragment.arguments = arguments

        if (mTwoPane) {
            fragment.fragmentManager.beginTransaction().replace(R.id.item_detail_container, noteFragment).commit()
        } else {
            fragment.fragmentManager.beginTransaction().add(R.id.item_master_container, noteFragment).addToBackStack(null).commit();
        }
    }

    fun onStart()
    {
        recyclerFileAdapter.refresh(fragment.activity);
    }

    fun destroy()
    {
        mCompositeSubscription.clear();
    }

}