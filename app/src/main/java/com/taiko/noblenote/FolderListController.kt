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
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding.view.clicks
import com.taiko.noblenote.document.SFile
import kotlinx.android.synthetic.main.fragment_file_list.view.*
import kotlinx.android.synthetic.main.fragment_twopane.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import java.io.File

/**
 * Created by taiko
 */
class FolderListController(private var fragment: Fragment, view: View) : LifecycleObserver {

    private var mTwoPane = false
    private val mCompositeSubscription = CompositeSubscription()


    private  val recyclerFileAdapter: RecyclerFileAdapter
    private val listSelectionController: ListSelectionController;

    init {
        mTwoPane = (fragment.parentFragment is TwoPaneFragment)

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

        if(mTwoPane)
        {
            val mf = fragment.parentFragment as TwoPaneFragment;
            listSelectionController = ListSelectionController(mf,mf.coordinator_layout, recyclerFileAdapter)

            recyclerFileAdapter.selectFolderOnClick =true
            mCompositeSubscription += recyclerFileAdapter.selectedFolder().subscribe {
                if(it == RecyclerView.NO_POSITION)
                {
                    val noteFragment = fragment.parentFragmentManager.findFragmentById(R.id.item_detail_container);
                    if(noteFragment != null)
                    {
                        fragment.parentFragmentManager.beginTransaction().remove(noteFragment).commit();
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
        else
        {
            listSelectionController = ListSelectionController(fragment, view, recyclerFileAdapter)
            view.appbar.visibility = View.VISIBLE;
            view.fab.visibility = View.VISIBLE;
            view.toolbar.inflateMenu(R.menu.menu_main)

            view.toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_settings -> {
                        fragment.findNavController().navigate(R.id.preferenceFragment);
                    }
                    R.id.action_search ->
                    {
                        fragment.findNavController().navigate(R.id.findInFilesFragment);
                    }
                }
                true;
            }

            mCompositeSubscription += listSelectionController.itemClicks()
                    .subscribe {
                        val item = recyclerFileAdapter.getItem(it);
                        if (item != null) {
                            Pref.currentFolderPath.onNext(item.uri.toString())
                            showNoteFragment(item.uri.toString())
                        }
                    }
        }
        listSelectionController.isTwoPane = mTwoPane;

        val app = (fragment.requireActivity().application as MainApplication)

        mCompositeSubscription += view.fab.clicks().subscribe {
            Dialogs.showNewFolderDialog(view, {app.eventBus.createFolderClick.onNext(it)})

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


        val arguments = Bundle()
        arguments.putString(NoteListFragment.ARG_FOLDER_PATH, folderUriString)


        if (mTwoPane) {
            val noteFragment = NoteListFragment()
            noteFragment.arguments = arguments
            fragment.parentFragmentManager.beginTransaction().replace(R.id.item_detail_container, noteFragment).commit()
            fragment.requireParentFragment().toolbar.title = null; // clear title after orientation change
            val pasteFileMenuItem = fragment.activity?.toolbar?.menu?.findItem(R.id.action_paste);
            pasteFileMenuItem?.isEnabled = mTwoPane && FileClipboard.hasContent;
        } else {
            fragment.findNavController().navigate(R.id.noteListFragment,arguments);
            fragment.toolbar.title = SFile(folderUriString).nameWithoutExtension
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