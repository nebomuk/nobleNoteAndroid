package com.taiko.noblenote

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_file_list.*
import kotlinx.android.synthetic.main.fragment_file_list.view.*
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.plusAssign
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.io.FileFilter


class NoteListFragment : Fragment() {


    private var mTwoPane = false


    private lateinit var recyclerFileAdapter: RecyclerFileAdapter

    private var mCompositeSubscription: CompositeSubscription = CompositeSubscription()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setHasOptionsMenu(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_file_list, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val rv = view.recycler_view
        //rv.itemAnimator = SlideInLeftAnimator();

        val fileFilter = FileFilter { pathname -> pathname.isFile && !pathname.isHidden }

        recyclerFileAdapter = RecyclerFileAdapter()
        recyclerFileAdapter.filter = fileFilter

        if (arguments.containsKey(MainActivity.ARG_TWO_PANE)) {
            mTwoPane = arguments.getBoolean(MainActivity.ARG_TWO_PANE)
        }

        if(arguments.containsKey(ARG_QUERY_TEXT))
        {
            tv_file_list_empty.setText(R.string.no_results_found);
            tv_title_search_results.visibility = View.VISIBLE;

            val queryText = arguments.getString(ARG_QUERY_TEXT,"");
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

                                        tv_file_list_empty.visibility = if(recyclerFileAdapter.itemCount == 0) View.VISIBLE else View.GONE;
                                })
            }
        }
        else // use current folder path and display the contents
        {
            tv_file_list_empty.setText(R.string.notebook_is_empty);

            mCompositeSubscription += Pref.currentFolderPath.subscribe { recyclerFileAdapter.path = File(it) }
        }

        rv.adapter = recyclerFileAdapter
        rv.layoutManager = LinearLayoutManager(activity)

        val app = (activity.application as MainApplication)

        val listController = ListController(activity as MainActivity,rv)
        listController.isHtmlActionAvailable = true;


        mCompositeSubscription += listController.itemClicks()
                .doOnNext { Log.d("","item pos clicked: " + it) }
                .subscribe { app.uiCommunicator.fileSelected.onNext(recyclerFileAdapter.getItem(it)) }

        app.uiCommunicator.createFileClick.subscribe { recyclerFileAdapter.addFile(it) }

        mCompositeSubscription += app.uiCommunicator.swipeRefresh.subscribe( {
            if(activity != null)
            {
                recyclerFileAdapter.refresh(activity)
            }

        },
        {
            KLog.e("exception in swipe refresh",it);

        });
    }

    override fun onStart() {
        super.onStart()
        recyclerFileAdapter.refresh(activity);
    }

    companion object {

        @JvmStatic
        val ARG_FOLDER_PATH = "folder_path" // used to display the contents of a folder

        @JvmStatic
        val ARG_QUERY_TEXT = "query_text" // used to display results of a full text search

        // start the note editor
        @JvmStatic
        fun startNoteEditor(activity: Context, file: File, argOpenMode : String, argQueryText : String = "") {
            if(!file.isFile)
            {
                KLog.w("startNoteEditor failed: $file is not a text file");
                return
            }

            val intent = Intent(activity, NoteEditorActivity::class.java)
            intent.putExtra(NoteEditorActivity.ARG_FILE_PATH, file.path)
            intent.putExtra(NoteEditorActivity.ARG_OPEN_MODE, argOpenMode)
            intent.putExtra(NoteEditorActivity.ARG_QUERY_TEXT,argQueryText);
            activity.startActivity(intent);
        }
    }


}
