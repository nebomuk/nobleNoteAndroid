package com.taiko.noblenote

import android.content.Intent
import android.database.DataSetObserver
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.view.MenuItem
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.view.focusChanges
import com.taiko.noblenote.document.SFile
import com.taiko.noblenote.editor.EditorActivity
import com.taiko.noblenote.preferences.PreferencesActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import rx.Observable
import rx.Subscription
import rx.lang.kotlin.plusAssign
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import rx.subscriptions.Subscriptions
import java.util.concurrent.TimeUnit



/***
 * controller class for the app's toolbar
 */
class MainToolbarController(val activity: MainActivity) {



    private val mCompositeSubscription: CompositeSubscription = CompositeSubscription();

    private lateinit var mSearchAdapter: ArrayAdapter<SFile>


    init {

        activity.toolbar.menu.findItem(R.id.action_settings)
                .setOnMenuItemClickListener{

                    // fixes issues when another root path has been selected and a folder is still open
                    if(!activity.twoPane && activity.supportFragmentManager.backStackEntryCount > 0)
                    {
                        activity.supportFragmentManager.popBackStack();
                    }
                    activity.startActivity(Intent(activity, PreferencesActivity::class.java))
                    true
                }

        val pasteFileMenuItem = activity.toolbar.menu.findItem(R.id.action_paste);

        mCompositeSubscription += addPasteFileListener(pasteFileMenuItem);

        activity.supportFragmentManager.addOnBackStackChangedListener {


            log.v("BackstackEntryCount: ${activity.supportFragmentManager.backStackEntryCount}")

            if(activity.supportFragmentManager.backStackEntryCount > 0)
            {

                val fragment = activity.supportFragmentManager.findFragmentById(R.id.item_master_container);
                val folderPath = fragment?.arguments?.getString(NoteListFragment.ARG_FOLDER_PATH,null)
                if(folderPath == null)
                {
                    log.e("Fragment argument ARG_FOLDER_PATH is null in mainToolbarControlller in onBackStackChangedListener");
                    return@addOnBackStackChangedListener;
                }
                activity.toolbar.title = SFile(folderPath).name;

                setBackNavigationIconEnabled(true)
                pasteFileMenuItem.isEnabled = FileClipboard.hasContent;

                log.v("addPasteFileListener folderPath: $folderPath");

            }
            else
            {
                setBackNavigationIconEnabled(false)
                pasteFileMenuItem.isEnabled = activity.twoPane && FileClipboard.hasContent; // disabled when not two pane and back stack 0 (folders visible)

                activity.toolbar.title = null;
            }
        }


        initSearch();
    }

    private fun addPasteFileListener(item : MenuItem): Subscription {

        return item.clicks().subscribe {
            val folderPath = Pref.currentFolderPath.value;


            if(!FileClipboard.pasteContentIntoFolder(SFile(folderPath)))
            {
                Snackbar.make(activity.coordinator_layout,R.string.msg_paste_error, Snackbar.LENGTH_LONG).show();
            }
            item.isEnabled = FileClipboard.hasContent;

        }
    }

    private fun setBackNavigationIconEnabled(value : Boolean)
    {
        if(value)
        {
            activity.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            activity.toolbar.setNavigationOnClickListener {
                activity.supportFragmentManager.popBackStack()
            }
        }
        else
        {
            activity.toolbar.navigationIcon = null;
            activity.toolbar.setNavigationOnClickListener(null)
        }
    }


    private fun initSearch() {

        val actionSearch = activity.toolbar.menu.findItem(R.id.action_search)
        mCompositeSubscription += actionSearch.clicks().subscribe { activity.mainViewModel.onActionSearchClick() }

        //MainActivity.startNoteEditor(activity,item, EditorActivity.READ_WRITE, activity.search_view.queryText.toString());

    }

    companion object
    {
        val log = loggerFor();
    }

    fun clearSubscriptions()
    {
        mCompositeSubscription.clear();
    }
}