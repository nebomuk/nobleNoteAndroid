package com.taiko.noblenote

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding.view.clicks
import com.taiko.noblenote.adapters.RecyclerFileAdapter
import com.taiko.noblenote.databinding.FragmentFileListBinding
import com.taiko.noblenote.filesystem.SFile
import com.taiko.noblenote.fragments.NoteListFragment
import com.taiko.noblenote.fragments.TwoPaneFragment
import com.taiko.noblenote.util.loggerFor
import rx.Subscription

import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import rx.subscriptions.Subscriptions
import java.io.File

/**
 * Created by taiko
 */
class FolderListController(private val fragment: Fragment, private val binding: FragmentFileListBinding) : LifecycleObserver {


    private var mVolumeSubscription: Subscription = Subscriptions.empty()
    private val log = loggerFor()
    private var mTwoPane = false
    private val mCompositeSubscription = CompositeSubscription()

    private  val recyclerFileAdapter: RecyclerFileAdapter
    private val listSelectionController: ListSelectionController;

    init {
        mTwoPane = (fragment.parentFragment is TwoPaneFragment)

        val recyclerView = binding.recyclerView;

        recyclerView.itemAnimator = DefaultItemAnimator();

        if(Uri.parse(Pref.rootPath.value).scheme == "file")
        {
            val dir = File(Pref.rootPath.value)
            if (!dir.exists())
                dir.mkdirs()
        }

        recyclerFileAdapter = RecyclerFileAdapter(SFile(Pref.rootPath.value))

        recyclerFileAdapter.showFolders = true;

        mCompositeSubscription += recyclerFileAdapter.applyEmptyView(binding.emptyListSwitcher,R.id.tv_recycler_view_empty,R.id.recycler_view)

        recyclerView.adapter = recyclerFileAdapter
        recyclerView.layoutManager = LinearLayoutManager(fragment.activity)

        val app = (fragment.requireActivity().application as MainApplication)


        if(mTwoPane)
        {

            listSelectionController = ListSelectionController(fragment.requireParentFragment().requireView(), recyclerFileAdapter, getTwoPaneToolbar())

            mCompositeSubscription +=  listSelectionController.fabVisible.subscribe{ app.eventBus.fabMenuVisible.onNext(it) }

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
                        showNoteListFragment(item.uri.toString())
                    }
                }
            }

            mCompositeSubscription += app.eventBus.swipeRefresh.subscribe {
                SFile.invalidateAllFileListCaches();
                recyclerFileAdapter.refresh()
            };

            binding.swipeRefresh.isEnabled = false;
        }
        else
        {
            listSelectionController = ListSelectionController(fragment.requireView(), recyclerFileAdapter,binding.toolbarInclude.toolbar)
            binding.appbar.visibility = View.VISIBLE;
            binding.fab.visibility = View.VISIBLE;
            listSelectionController.fabVisible.subscribe { binding.fab.visibility = it }


            binding.toolbarInclude.toolbar.title = fragment.getString(R.string.myNotebooks)
            binding.toolbarInclude.toolbar.inflateMenu(R.menu.menu_main)

            binding.toolbarInclude.toolbar.setOnMenuItemClickListener {
                when (it.itemId) {

                    R.id.action_settings -> {
                    fragment.findNavController().navigate(R.id.preferenceFragment);
                    }
                    R.id.action_search ->
                    {
                        fragment.findNavController().navigate(R.id.findInFilesFragment);
                    }
                }
                FileClipboard.clearContent();

                true;
            }

            mCompositeSubscription += listSelectionController.itemClicks()
                    .subscribe {
                        val item = recyclerFileAdapter.getItem(it);
                        if (item != null) {
                            Pref.currentFolderPath.onNext(item.uri.toString())
                            showNoteListFragment(item.uri.toString())
                        }
                    }

            binding.swipeRefresh.setOnRefreshListener {
                Handler(Looper.getMainLooper()).postDelayed({binding.swipeRefresh.isRefreshing = false},500)
                SFile.invalidateAllFileListCaches();
                recyclerFileAdapter.refresh()
            }


        }
        listSelectionController.isTwoPane = mTwoPane;

        mCompositeSubscription += binding.fab.clicks().subscribe {
            Dialogs.showNewFolderDialog(binding.root, {app.eventBus.createFolderClick.onNext(it)})

            FileClipboard.clearContent();
        }

        mCompositeSubscription += app.eventBus.createFolderClick.subscribe { recyclerFileAdapter.refresh()}
    }

    private fun showNoteListFragment(folderUriString: String) {
        val arguments = Bundle()
        arguments.putString(NoteListFragment.ARG_FOLDER_PATH, folderUriString)

        if (mTwoPane) {
            val noteFragment = NoteListFragment()
            noteFragment.arguments = arguments
            fragment.parentFragmentManager.beginTransaction().replace(R.id.item_detail_container, noteFragment).commit()
            val pasteFileMenuItem = getTwoPaneToolbar().menu?.findItem(R.id.action_paste);
            pasteFileMenuItem?.isVisible = mTwoPane && FileClipboard.hasContent;
        } else {
            fragment.findNavController().navigate(R.id.action_folderListFragment_to_noteListFragment,arguments);
        }
    }

    private fun getTwoPaneToolbar() : Toolbar
    {
        return fragment.requireParentFragment().requireView().findViewById(R.id.toolbarTwoPane)!!;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart()
    {
            recyclerFileAdapter.path = SFile(Pref.rootPath.value);
            recyclerFileAdapter.refresh();

        mVolumeSubscription = VolumeNotAccessibleDialog.showAutomatically(fragment);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop()
    {
        mVolumeSubscription.unsubscribe();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroyView()
    {
        mCompositeSubscription.clear();
        listSelectionController.clearSubscriptions()
    }



}