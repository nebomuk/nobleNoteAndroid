package com.taiko.noblenote

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.fragment_file_list.view.*
import kotlinx.android.synthetic.main.toolbar.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.io.FileFilter

/**
 * Created by taiko
 */
class FolderListController(private var fragment: Fragment, view: View) {

    private var mTwoPane = false
    private val mCompositeSubscription = CompositeSubscription()


    private  val recyclerFileAdapter: RecyclerFileAdapter
    private val listSelectionController: ListSelectionController;

    init {
        mTwoPane = (fragment.activity as MainActivity).twoPane

        val recyclerView = view.recycler_view;

        recyclerView.itemAnimator = DefaultItemAnimator();

        val dir = File(Pref.rootPath.value)
        if (!dir.exists())
            dir.mkdirs()

        // the following code lists only visible folders and push their names into an ArrayAdapter
        val folderFilter = FileFilter { pathname -> pathname.isDirectory && !pathname.isHidden }

        recyclerFileAdapter = RecyclerFileAdapter(File(Pref.rootPath.value))

        recyclerFileAdapter.filter = folderFilter

        recyclerFileAdapter.applyEmptyView(view.empty_list_switcher,R.id.text_empty,R.id.recycler_view)

        recyclerView.adapter = recyclerFileAdapter
        recyclerView.layoutManager = LinearLayoutManager(fragment.activity)

        listSelectionController =ListSelectionController(fragment.activity as MainActivity, recyclerFileAdapter)
        listSelectionController.isTwoPane = mTwoPane;

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
            mCompositeSubscription += listSelectionController.itemClicks()
                    .subscribe {
                        val item = recyclerFileAdapter.getItem(it);
                        if (item != null) {
                            Pref.currentFolderPath.onNext(item.absolutePath)
                            showNoteFragment(item.absolutePath)
                        }

                    }
        }


        mCompositeSubscription += app.eventBus.createFolderClick.subscribe { recyclerFileAdapter.addFileName(it.name) }

        mCompositeSubscription += app.eventBus.swipeRefresh.subscribe {
            if (fragment != null) {
                recyclerFileAdapter.refresh(fragment.activity)
            }
        };
    }

    private fun showNoteFragment(folderPath: String) {

        val noteFragment = NoteListFragment()
        val arguments = Bundle()
        arguments.putString(NoteListFragment.ARG_FOLDER_PATH, folderPath)
        noteFragment.arguments = arguments

        if (mTwoPane) {
            fragment.fragmentManager.beginTransaction().replace(R.id.item_detail_container, noteFragment).commit()
            fragment.activity.toolbar.title = null; // clear title after orientation change
        } else {
            fragment.fragmentManager.beginTransaction().add(R.id.item_master_container, noteFragment).addToBackStack(null).commit();
            fragment.activity.toolbar.title = File(folderPath).nameWithoutExtension
        }
    }

    fun onStart()
    {
        mCompositeSubscription += Pref.rootPath.subscribe {
            recyclerFileAdapter.path = File(it);
            recyclerFileAdapter.refresh(fragment.activity); }
    }

    fun onDestroyView()
    {
        mCompositeSubscription.clear();
        listSelectionController.clearSubscriptions()
    }



}