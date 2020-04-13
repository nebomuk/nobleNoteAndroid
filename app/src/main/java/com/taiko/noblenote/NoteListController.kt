package com.taiko.noblenote

import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.taiko.noblenote.document.SFile
import kotlinx.android.synthetic.main.fragment_file_list.view.*
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.plusAssign
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.FileFilter

class NoteListController(private var fragment: Fragment, view: View)
    : LifecycleObserver {

    private val recyclerFileAdapter: RecyclerFileAdapter
    private val listSelectionController : ListSelectionController

    private var mCompositeSubscription: CompositeSubscription = CompositeSubscription()

    init {

        val recyclerView = view.recycler_view
        //recyclerView.itemAnimator = SlideInLeftAnimator();

        val fileFilter = FileFilter { pathname -> pathname.isFile && !pathname.isHidden }


        var path : SFile;



        // FIXME search submit is currently broken because this is not working
        if(fragment.arguments?.containsKey(NoteListFragment.ARG_QUERY_TEXT) == true)
        {
            // file path of the adapter is not used, because it is only used to display search results
            recyclerFileAdapter = RecyclerFileAdapter(SFile(Pref.rootPath.value));

            displaySearchResults(view)
        }
        else
        {

            path = SFile(fragment.arguments?.getString(NoteListFragment.ARG_FOLDER_PATH));

            recyclerFileAdapter = RecyclerFileAdapter(path)
//            recyclerFileAdapter.refresh(activity)
        }


        recyclerFileAdapter.showFolders = false

        mCompositeSubscription += recyclerFileAdapter.applyEmptyView(view.empty_list_switcher,R.id.tv_recycler_view_empty,R.id.recycler_view)

        recyclerView.adapter = recyclerFileAdapter
        recyclerView.layoutManager = LinearLayoutManager(fragment.activity)

        val app = (fragment.activity?.application as MainApplication)

        listSelectionController = ListSelectionController(fragment.activity as MainActivity,recyclerFileAdapter)
        listSelectionController.isNoteList = true;


        mCompositeSubscription += listSelectionController.itemClicks()
                .doOnNext { Log.d("","item pos clicked: " + it) }
                .subscribe { app.eventBus.fileSelected.onNext(recyclerFileAdapter.getItem(it)) }

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
    }

    private fun displaySearchResults(view: View) {
        view.tv_file_list_empty.setText(R.string.no_results_found);
        view.tv_title_search_results.visibility = View.VISIBLE;

        val queryText = fragment.arguments!!.getString(NoteListFragment.ARG_QUERY_TEXT, "");
        if (!queryText.isNullOrBlank()) {
            mCompositeSubscription += FindInFiles.recursiveFullTextSearch(SFile(Pref.rootPath.value), queryText)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        recyclerFileAdapter.addFileName(it.name)
                    },

                            {},
                            // on Completed
                            {
                                // show "no results" text

                                view.tv_file_list_empty.visibility = if (recyclerFileAdapter.itemCount == 0) View.VISIBLE else View.GONE;
                            })
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