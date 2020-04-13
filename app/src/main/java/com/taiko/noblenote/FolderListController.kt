package com.taiko.noblenote

import android.net.Uri
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.taiko.noblenote.document.SFile
import kotlinx.android.synthetic.main.fragment_file_list.view.*
import kotlinx.android.synthetic.main.toolbar.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.io.FileFilter

/**
 * Created by taiko
 */
class FolderListController(private var fragment: Fragment, view: View) : LifecycleObserver {

    private var mTwoPane = false
    private val mCompositeSubscription = CompositeSubscription()


    private  val recyclerFileAdapter: RecyclerFileAdapter
    private val listSelectionController: ListSelectionController;

    init {
        mTwoPane = (fragment.activity as MainActivity).twoPane

        val recyclerView = view.recycler_view;

        recyclerView.itemAnimator = DefaultItemAnimator();

        if(Uri.parse(Pref.rootPath.value).scheme == "file")
        {
            val dir = File(Pref.rootPath.value)
            if (!dir.exists())
                dir.mkdirs()
        }


        recyclerFileAdapter = RecyclerFileAdapter(SFile(Pref.rootPath.value))

        recyclerFileAdapter.showFolders = true;

        mCompositeSubscription += recyclerFileAdapter.applyEmptyView(view.empty_list_switcher,R.id.tv_recycler_view_empty,R.id.recycler_view)

        recyclerView.adapter = recyclerFileAdapter
        recyclerView.layoutManager = LinearLayoutManager(fragment.activity)

        listSelectionController =ListSelectionController(fragment.activity as MainActivity, recyclerFileAdapter)
        listSelectionController.isTwoPane = mTwoPane;

        val app = (fragment.activity!!.application as MainApplication)

        if(mTwoPane)
        {
            recyclerFileAdapter.selectFolderOnClick =true
            mCompositeSubscription += recyclerFileAdapter.selectedFolder().subscribe {
                if(it == RecyclerView.NO_POSITION)
                {
                    val noteFragment = fragment.fragmentManager?.findFragmentById(R.id.item_detail_container);
                    if(noteFragment != null)
                    {
                        fragment.fragmentManager?.beginTransaction()?.remove(noteFragment)?.commit();
                    }
                }
                else
                {
                    val item = recyclerFileAdapter.getItem(it)
                    if(item != null) {
                        Pref.currentFolderPath.onNext(item.uri.toString())
                        showNoteFragment(item.uri.toString())
                    }

                }
            }

        }
        else {
            mCompositeSubscription += listSelectionController.itemClicks()
                    .subscribe {
                        val item = recyclerFileAdapter.getItem(it);
                        if (item != null) {
                            Pref.currentFolderPath.onNext(item.uri.toString())
                            showNoteFragment(item.uri.toString())
                        }

                    }
        }


        mCompositeSubscription += app.eventBus.createFolderClick.subscribe { recyclerFileAdapter.addFileName(it.name) }

        mCompositeSubscription += app.eventBus.swipeRefresh.subscribe {
            if (fragment != null) {
                SFile.invalidateAllFileListCaches();
                recyclerFileAdapter.refresh()
            }
        };
    }

    private fun showNoteFragment(folderUriString: String) {

        val noteFragment = NoteListFragment()
        val arguments = Bundle()
        arguments.putString(NoteListFragment.ARG_FOLDER_PATH, folderUriString)
        noteFragment.arguments = arguments

        if (mTwoPane) {
            fragment.fragmentManager?.beginTransaction()?.replace(R.id.item_detail_container, noteFragment)?.commit()
            fragment.activity?.toolbar!!.title = null; // clear title after orientation change
            val pasteFileMenuItem = fragment.activity?.toolbar?.menu?.findItem(R.id.action_paste);
            pasteFileMenuItem?.isEnabled = mTwoPane && FileClipboard.hasContent;
        } else {
            fragment.fragmentManager!!.beginTransaction().add(R.id.item_master_container, noteFragment).addToBackStack(null).commit();
            fragment.activity!!.toolbar.title = SFile(folderUriString).nameWithoutExtension
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart()
    {
            recyclerFileAdapter.path = SFile(Pref.rootPath.value);
            recyclerFileAdapter.refresh();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroyView()
    {
        mCompositeSubscription.clear();
        listSelectionController.clearSubscriptions()
    }



}