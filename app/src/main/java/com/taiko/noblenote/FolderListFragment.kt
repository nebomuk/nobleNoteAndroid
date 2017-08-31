package com.taiko.noblenote

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import kotlinx.android.synthetic.main.fragment_file_list.view.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.io.FileFilter






class FolderListFragment : Fragment() {

    private val mActivatedPosition = ListView.INVALID_POSITION
    private var mTwoPane = false
    private val mCompositeSubscription = CompositeSubscription()


    private lateinit var recyclerFileAdapter: RecyclerFileAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_file_list, container, false)

        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        mTwoPane = (activity as MainActivity).twoPane

        val rv = view?.recycler_view as RecyclerView
        rv.itemAnimator = DefaultItemAnimator();

        val dir = File(Pref.rootPath.value)
        if (!dir.exists())
            dir.mkdirs()

        // the following code lists only visible folders and push their names into an ArrayAdapter
        val folderFilter = FileFilter { pathname -> pathname.isDirectory && !pathname.isHidden }

        recyclerFileAdapter = RecyclerFileAdapter()
        recyclerFileAdapter.filter = folderFilter

        rv.adapter = recyclerFileAdapter
        rv.layoutManager = LinearLayoutManager(activity)

        val listController = ListSelectionController(activity as MainActivity,rv)
        listController.isTwoPaneFolderList = mTwoPane;

        val app = (activity.application as MainApplication)

        if(mTwoPane)
        {
            recyclerFileAdapter.selectFolderOnClick =true
            mCompositeSubscription += recyclerFileAdapter.selectedFolder().subscribe {
                if(it == RecyclerView.NO_POSITION)
                {
                    val noteFragment = fragmentManager.findFragmentById(R.id.item_detail_container);
                    if(noteFragment != null)
                    {
                        fragmentManager.beginTransaction().remove(noteFragment).commit();
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
            mCompositeSubscription += listController.itemClicks()
                    .subscribe {
                        val item = recyclerFileAdapter.getItem(it);
                        if (item != null) {
                            Pref.currentFolderPath.onNext(item.absolutePath)
                            showNoteFragment(item.absolutePath)
                        }

                    }
        }



        mCompositeSubscription += app.eventBus.createFolderClick.subscribe { recyclerFileAdapter.addFile(it) }

        mCompositeSubscription += app.eventBus.swipeRefresh.subscribe( {
            if (activity != null) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        mCompositeSubscription.clear()

    }

    fun showNoteFragment(folderPath : String) {

        val fragment = NoteListFragment()
        val arguments = Bundle()
        arguments.putString(NoteListFragment.ARG_FOLDER_PATH,folderPath)
        fragment.arguments = arguments

        if (mTwoPane) {
            fragmentManager.beginTransaction().replace(R.id.item_detail_container, fragment).commit()
        } else {
            fragmentManager.beginTransaction().add(R.id.item_master_container, fragment).addToBackStack(null).commit();
        }
    }




}
