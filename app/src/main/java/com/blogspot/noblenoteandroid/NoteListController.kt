package com.blogspot.noblenoteandroid

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding.view.clicks
import com.blogspot.noblenoteandroid.adapters.RecyclerFileAdapter
import com.blogspot.noblenoteandroid.databinding.FragmentFileListBinding
import com.blogspot.noblenoteandroid.filesystem.SFile
import com.blogspot.noblenoteandroid.fragments.EditorFragment
import com.blogspot.noblenoteandroid.extensions.createNoteEditorArgs
import com.blogspot.noblenoteandroid.fragments.NoteListFragment
import com.blogspot.noblenoteandroid.fragments.TwoPaneFragment
import com.blogspot.noblenoteandroid.util.loggerFor
import rx.Subscription
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import rx.subscriptions.Subscriptions
import java.lang.IllegalStateException

class NoteListController(private var fragment: Fragment, binding: FragmentFileListBinding)
    : DefaultLifecycleObserver {

    private var mTwoPane: Boolean
    private var mVolumeSubscription: Subscription = Subscriptions.empty()
    private val recyclerFileAdapter: RecyclerFileAdapter
    private val listSelectionController : ListSelectionController

    private var mCompositeSubscription: CompositeSubscription = CompositeSubscription()

    init {

        val recyclerView = binding.recyclerView

        val path  = fragment.requireArguments().getString(NoteListFragment.ARG_FOLDER_PATH)
                ?: throw IllegalStateException("ARG_FOLDER_PATH is null");

        recyclerFileAdapter = RecyclerFileAdapter(SFile(path))


        recyclerFileAdapter.showFolders = false

        mCompositeSubscription += recyclerFileAdapter.applyEmptyView(binding.emptyListSwitcher,R.id.tv_recycler_view_empty,R.id.recycler_view)

        recyclerView.adapter = recyclerFileAdapter
        recyclerView.layoutManager = LinearLayoutManager(fragment.activity)

        val app = (fragment.activity?.application as MainApplication)


        mTwoPane = (fragment.parentFragment is TwoPaneFragment)

        if(mTwoPane)
        {
            listSelectionController = ListSelectionController(fragment.requireParentFragment().requireView(), recyclerFileAdapter,binding.toolbarInclude.toolbar)

            mCompositeSubscription +=  listSelectionController.fabVisible.subscribe{ app.eventBus.fabMenuVisible.onNext(it) }

            mCompositeSubscription += app.eventBus.swipeRefresh.subscribe( {
                recyclerFileAdapter.refresh()
            }, {
                        log.e("exception in swipe refresh",it);
                    });
            binding.swipeRefresh.isEnabled = false;
        }
        else
        {
            listSelectionController = ListSelectionController(fragment.requireView(),  recyclerFileAdapter,binding.toolbarInclude.toolbar)
            binding.appbar.visibility = View.VISIBLE;
            binding.fab.visibility = View.VISIBLE;

            listSelectionController.fabVisible.subscribe { binding.fab.visibility = it }

            binding.toolbarInclude.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            binding.toolbarInclude.toolbar.setNavigationOnClickListener {
                fragment.findNavController().navigateUp();
            }

            val folderPath = fragment.arguments?.getString(NoteListFragment.ARG_FOLDER_PATH,null)
            binding.toolbarInclude.toolbar.title = folderPath?.let { SFile(it).name };
            binding.toolbarInclude.toolbar.inflateMenu(R.menu.menu_paste);

            if(FileClipboard.hasContent)
            {
                val pasteItem = binding.toolbarInclude.toolbar.menu.findItem(R.id.action_paste);
                pasteItem.isVisible = true;
                pasteItem
                        .setOnMenuItemClickListener {

                            if(!FileClipboard.pasteContentIntoFolder(SFile(folderPath!!)))
                            {
                                Snackbar.make(binding.root,R.string.msg_paste_error, Snackbar.LENGTH_LONG).show();
                            }
                            pasteItem.isVisible = FileClipboard.hasContent;
                            true;
                        }
            }

            binding.swipeRefresh.setOnRefreshListener {
                Handler(Looper.getMainLooper()).postDelayed({binding.swipeRefresh.isRefreshing = false},500)
                SFile.invalidateAllFileListCaches();
                recyclerFileAdapter.refresh()
            }

        }
        listSelectionController.isNoteList = true;

        mCompositeSubscription += listSelectionController.showHtml.subscribe {
            fragment.findNavController().navigate(R.id.editorFragment,createNoteEditorArgs(file = it, argOpenMode = EditorFragment.HTML, argQueryText = ""))
        }


        mCompositeSubscription += listSelectionController.itemClicks()
                .doOnNext { Log.d("","item pos clicked: " + it) }
                .subscribe { app.eventBus.fileSelected.onNext(recyclerFileAdapter.getItem(it)) }

        mCompositeSubscription += app.eventBus.fileSelected.mergeWith(app.eventBus.createFileClick)
                .subscribe { fragment.findNavController().navigate(R.id.editorFragment,
                        createNoteEditorArgs(file = it, argOpenMode = EditorFragment.READ_WRITE, argQueryText = "")) }


        mCompositeSubscription += app.eventBus.createFileClick.subscribe { recyclerFileAdapter.refresh() }


        mCompositeSubscription += FileClipboard.pastedFileNames.subscribe {
            recyclerFileAdapter.refresh()
        }

        mCompositeSubscription += binding.fab.clicks().subscribe {
                Dialogs.showNewNoteDialog(binding.root, currentFolderPath = path, fileCreated = {app.eventBus.createFileClick.onNext(it)})

                FileClipboard.clearContent();
                binding.toolbarInclude.toolbar.menu.findItem(R.id.action_paste)?.isVisible = false;
            }
    }

    override fun onStart(owner: LifecycleOwner)
    {
        SFile.invalidateAllFileListCaches();
        recyclerFileAdapter.refresh();

        if(!mTwoPane)
        {
            mVolumeSubscription = VolumeNotAccessibleDialog.showAutomatically(fragment);
        }
    }

    override fun onStop(owner: LifecycleOwner)
    {
        mVolumeSubscription.unsubscribe();
    }

    override fun onDestroy(owner: LifecycleOwner)
    {
        mCompositeSubscription.clear();
        listSelectionController.clearSubscriptions()
    }

    companion object {
        private val log = loggerFor()
    }
}
