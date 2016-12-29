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
import kotlinx.android.synthetic.main.fragment_file_list.view.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.io.FileFilter


class NoteListFragment : Fragment() {


    private var mTwoPane = false

    private var folderPath: String? = null

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

        if (arguments.containsKey(MainActivity.ARG_TWO_PANE)) {
            mTwoPane = arguments.getBoolean(MainActivity.ARG_TWO_PANE)
        }
        if (arguments.containsKey(ARG_FOLDER_PATH)) {
            folderPath = arguments.getString(ARG_FOLDER_PATH)
        }
        val fileFilter = FileFilter { pathname -> pathname.isFile && !pathname.isHidden }

        recyclerFileAdapter = RecyclerFileAdapter()
        recyclerFileAdapter.filter = fileFilter
        recyclerFileAdapter.path = File(folderPath)

        rv.adapter = recyclerFileAdapter
        rv.layoutManager = LinearLayoutManager(activity)



        val app = (activity.application as MainApplication)
        mCompositeSubscription += recyclerFileAdapter.itemClicks()
                .doOnNext { Log.d("","item pos clicked: " + it) }
                .subscribe { app.uiCommunicator.fileSelected.onNext(recyclerFileAdapter.getItem(it)) }

        app.uiCommunicator.fileCreated.subscribe { recyclerFileAdapter.addFile(it) }
    }

    companion object {

        @JvmStatic
        val ARG_FOLDER_PATH = "folder_path"

        @JvmStatic
        fun startNoteEditor(activity: Context, file: File) {
            val detailIntent = Intent(activity, NoteEditorActivity::class.java)
            detailIntent.putExtra(NoteEditorActivity.ARG_FILE_PATH, file.path)
            detailIntent.putExtra(NoteEditorActivity.ARG_OPEN_MODE, NoteEditorActivity.READ_WRITE)
            activity.startActivity(detailIntent)
        }
    }


}
