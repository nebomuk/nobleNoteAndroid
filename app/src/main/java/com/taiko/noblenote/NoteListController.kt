package com.taiko.noblenote

import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding.view.clicks
import com.taiko.noblenote.document.SFile
import com.taiko.noblenote.editor.EditorFragment
import com.taiko.noblenote.extensions.createNoteEditorArgs
import kotlinx.android.synthetic.main.fragment_file_list.view.*
import kotlinx.android.synthetic.main.fragment_twopane.*
import kotlinx.android.synthetic.main.toolbar.view.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import java.lang.IllegalStateException

class NoteListController(private var fragment: Fragment, view: View)
    : LifecycleObserver {

    private val recyclerFileAdapter: RecyclerFileAdapter
    private val listSelectionController : ListSelectionController

    private var mCompositeSubscription: CompositeSubscription = CompositeSubscription()

    init {

        val recyclerView = view.recycler_view
        //recyclerView.itemAnimator = SlideInLeftAnimator();

        val path  = fragment.arguments?.getString(NoteListFragment.ARG_FOLDER_PATH)
                ?: throw IllegalStateException("ARG_FOLDER_PATH is null");

        recyclerFileAdapter = RecyclerFileAdapter(SFile(path))
//            recyclerFileAdapter.refresh(activity)


        recyclerFileAdapter.showFolders = false

        mCompositeSubscription += recyclerFileAdapter.applyEmptyView(view.empty_list_switcher,R.id.tv_recycler_view_empty,R.id.recycler_view)

        recyclerView.adapter = recyclerFileAdapter
        recyclerView.layoutManager = LinearLayoutManager(fragment.activity)

        val app = (fragment.activity?.application as MainApplication)


        val mTwoPane = (fragment.parentFragment is TwoPaneFragment)

        if(mTwoPane)
        {
            val mf = fragment.parentFragment as TwoPaneFragment;
            listSelectionController = ListSelectionController(mf,mf.coordinator_layout, recyclerFileAdapter)
        }
        else
        {
            listSelectionController = ListSelectionController(fragment, view, recyclerFileAdapter)
            view.appbar.visibility = View.VISIBLE;
            view.fab.visibility = View.VISIBLE;

            view.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            view.toolbar.setNavigationOnClickListener {
                fragment.findNavController().navigateUp();
            }

            val folderPath = fragment.arguments?.getString(NoteListFragment.ARG_FOLDER_PATH,null)
            view.toolbar.title = folderPath?.let { SFile(it).name };

            if(FileClipboard.hasContent)
            {
                view.toolbar.inflateMenu(R.menu.menu_paste);
                val pasteItem = view.toolbar.menu.findItem(R.id.action_paste);
                pasteItem
                        .setOnMenuItemClickListener {

                            if(!FileClipboard.pasteContentIntoFolder(SFile(folderPath!!)))
                            {
                                Snackbar.make(view,R.string.msg_paste_error, Snackbar.LENGTH_LONG).show();
                            }
                            pasteItem.isVisible = FileClipboard.hasContent;
                            true;
                        }
            }
        }
        listSelectionController.isNoteList = true;


        mCompositeSubscription += listSelectionController.itemClicks()
                .doOnNext { Log.d("","item pos clicked: " + it) }
                .subscribe { app.eventBus.fileSelected.onNext(recyclerFileAdapter.getItem(it)) }

        mCompositeSubscription += app.eventBus.fileSelected.mergeWith(app.eventBus.createFileClick)
                .subscribe { fragment.findNavController().navigate(R.id.editorFragment,createNoteEditorArgs(it,EditorFragment.READ_WRITE)) }


        mCompositeSubscription += app.eventBus.createFileClick.subscribe { recyclerFileAdapter.addFileName(it.name) }

        mCompositeSubscription += app.eventBus.swipeRefresh.subscribe( {
            if(fragment.activity != null)
            {
                recyclerFileAdapter.refresh()
            }
        },
                {
                    log.e("exception in swipe refresh",it);
                });


        mCompositeSubscription += FileClipboard.pastedFileNames.subscribe {
            for (fileName : String in it)
            {
                recyclerFileAdapter.addFileName(fileName)
            };
        }

        mCompositeSubscription += view.fab.clicks().subscribe {
                Dialogs.showNewNoteDialog(view, {app.eventBus.createFileClick.onNext(it)})
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart()
    {
        SFile.invalidateAllFileListCaches();
        recyclerFileAdapter.refresh();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroyView()
    {
        mCompositeSubscription.clear();
        listSelectionController.clearSubscriptions()
    }



    companion object {
        private val log = loggerFor()
    }
}