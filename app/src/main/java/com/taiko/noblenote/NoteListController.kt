package com.taiko.noblenote

import android.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.fragment_file_list.view.*
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.plusAssign
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.io.FileFilter

class NoteListController(private var fragment: Fragment, view: View)
{

    private lateinit var recyclerFileAdapter: RecyclerFileAdapter

    private var mCompositeSubscription: CompositeSubscription = CompositeSubscription()

    init {

        val rv = view.recycler_view
        //rv.itemAnimator = SlideInLeftAnimator();

        val fileFilter = FileFilter { pathname -> pathname.isFile && !pathname.isHidden }

        recyclerFileAdapter = RecyclerFileAdapter()
        recyclerFileAdapter.filter = fileFilter

        recyclerFileAdapter.applyEmptyView(view.empty_list_switcher,R.id.text_empty,R.id.recycler_view)


        if(fragment.arguments != null &&  fragment.arguments.containsKey(NoteListFragment.ARG_QUERY_TEXT))
        {
            view.tv_file_list_empty.setText(R.string.no_results_found);
            view.tv_title_search_results.visibility = View.VISIBLE;

            val queryText = fragment.arguments.getString(NoteListFragment.ARG_QUERY_TEXT,"");
            if (!queryText.isNullOrBlank()) {
                mCompositeSubscription += FindInFiles.findHtmlInFiles(Pref.rootPath.value,queryText)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( {
                            recyclerFileAdapter.addFile(File(it))
                        },

                                {},
                                // on Completed
                                {
                                    // show "no results" text

                                    view.tv_file_list_empty.visibility = if(recyclerFileAdapter.itemCount == 0) View.VISIBLE else View.GONE;
                                })
            }
        }
        else if(fragment.arguments != null && fragment.arguments.containsKey(NoteListFragment.ARG_FOLDER_PATH)) // use current folder path and display the contents
        {
            view.tv_file_list_empty.setText(R.string.notebook_is_empty);

            recyclerFileAdapter.path = File(fragment.arguments.getString(NoteListFragment.ARG_FOLDER_PATH));
//            recyclerFileAdapter.refresh(activity)
        }

        rv.adapter = recyclerFileAdapter
        rv.layoutManager = LinearLayoutManager(fragment.activity)

        val app = (fragment.activity.application as MainApplication)

        val listController = ListSelectionController(fragment.activity as MainActivity,rv)
        listController.isHtmlActionAvailable = true;


        mCompositeSubscription += listController.itemClicks()
                .doOnNext { Log.d("","item pos clicked: " + it) }
                .subscribe { app.eventBus.fileSelected.onNext(recyclerFileAdapter.getItem(it)) }

        mCompositeSubscription += app.eventBus.createFileClick.subscribe { recyclerFileAdapter.addFile(it) }

        mCompositeSubscription += app.eventBus.swipeRefresh.subscribe( {
            if(fragment.activity != null)
            {
                recyclerFileAdapter.refresh(fragment.activity)
            }
        },
                {
                    log.e("exception in swipe refresh",it);
                });
    }



    fun onStart()
    {
        recyclerFileAdapter.refresh(fragment.activity);
    }

    fun onDestroyView()
    {

        mCompositeSubscription.clear();
    }



    companion object {
        private val log = loggerFor()
    }
}