package com.taiko.noblenote

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.transition.Fade
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Toast
import com.github.developerpaul123.filepickerlibrary.FilePickerActivity
import com.github.developerpaul123.filepickerlibrary.enums.Request
import com.github.developerpaul123.filepickerlibrary.enums.ThemeType
import com.jakewharton.rxbinding.support.v7.widget.queryTextChanges
import com.tbruyelle.rxpermissions.RxPermissions
import kotlinx.android.synthetic.main.toolbar.*
import rx.Observable
import rx.lang.kotlin.plusAssign
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import rx_activity_result.RxActivityResult
import java.io.File
import java.util.concurrent.TimeUnit


class MainActivity : Activity()
{

    private var mTwoPane: Boolean = false

    public companion object {
        @JvmField val ARG_TWO_PANE = "two_pane"
    }

    public fun onItemSelected(path: String) {

            val arguments = Bundle()
            arguments.putString(NoteListFragment.ARG_FOLDER_PATH, path)
            arguments.putBoolean(MainActivity.ARG_TWO_PANE, mTwoPane)
            val fragment = NoteListFragment()
            fragment.arguments = arguments


        if (mTwoPane) {
            fragmentManager.beginTransaction().replace(R.id.item_detail_container, fragment).commit()
        } else {
            fragmentManager.beginTransaction().add(R.id.item_master_container, fragment).addToBackStack("").commit();
        }
    }



    val compositeSubscription = CompositeSubscription()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setActionBar(toolbar)

    // https://stackoverflow.com/questions/26600263/how-do-i-prevent-the-status-bar-and-navigation-bar-from-animating-during-an-acti
        val fade = Fade()
        fade.excludeTarget(android.R.id.statusBarBackground, true)
        fade.excludeTarget(android.R.id.navigationBarBackground, true)
        window.exitTransition = fade
        window.enterTransition = fade

        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true
        }

//        setSupportActionBar(toolbar) // required to make styling working, activity options menu callbacks now have to be used


        // request permissions dialog
        compositeSubscription += RxPermissions.getInstance(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe {
            if(it) {
                val rootPath = Pref.rootPath + "/"
                val folderFrag = FolderListFragment()
               // folderFrag.setActivateOnItemClick(true) must be called when content view created
                fragmentManager.beginTransaction().add(R.id.item_master_container, folderFrag).commit()
//                supportFragmentManager.beginTransaction().add(R.id.content_left,NBFragment.newInstance(rootPath, NBFragment.FragmentRole.FRAGMENT_ROLE_FOLDER_LIST)).commit()
//                supportFragmentManager.beginTransaction().add(R.id.content_right,NBFragment.newInstance(Pref.selectedFolderPath, NBFragment.FragmentRole.FRAGMENT_ROLE_FOLDER_CONTENTS)).commit()

            }
        }

        val app = application as MainApplication
        compositeSubscription += app.uiCommunicator.folderSelected.subscribe { onItemSelected(it.absolutePath) }
    }

    private var actionSearch: MenuItem? = null



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.menu_main, menu);
        actionSearch = menu?.findItem(R.id.action_search)
        val searchView = actionSearch?.actionView as SearchView

        val searchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
        searchAutoComplete.threshold = 2
        val queryTextObs : Observable<CharSequence> = searchView.queryTextChanges()
                .share()
                .throttleWithTimeout(300, TimeUnit.MILLISECONDS, Schedulers.io())
                .filter { !it.isEmpty() }
                .distinctUntilChanged()
        searchAutoComplete.setAdapter(SuggestionAdapter(this, queryTextObs))
        searchAutoComplete.onItemClickListener = AdapterView.OnItemClickListener {

            adapterView, view, i, l -> Unit
            val item = adapterView.adapter.getItem(i) as File;
            NoteListFragment.startNoteEditor(this,item);
        }


        // requires some manifest stuff https://stackoverflow.com/questions/27378981/how-to-use-searchview-in-toolbar-android
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)


        // open file picker activity
        val itemId = item?.itemId

            // show directory chooser dialog
        if(itemId == R.id.action_rootPath)
        {
            val filePickerDialogIntent = Intent(this, FilePickerActivity::class.java)
            filePickerDialogIntent.putExtra(FilePickerActivity.THEME_TYPE, ThemeType.DIALOG);
            filePickerDialogIntent.putExtra(FilePickerActivity.REQUEST, Request.DIRECTORY);

            compositeSubscription += Observable.concat(RxPermissions.getInstance(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE).map {
                if(it) RxActivityResult.on(this@MainActivity).startIntent(filePickerDialogIntent) else Observable.empty()
            }).subscribe {

                if ((it.resultCode() == RESULT_OK))
                {
                    val path = it.data().getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH)
                    Pref.rootPath = path
                    Toast.makeText(this, "Directory Selected: " + Pref.rootPath, Toast.LENGTH_LONG).show();

                }
            }
        }
        return true;
    }

    override fun onBackPressed() {
//         open close search view

        // close search
        if (actionSearch!!.isActionViewExpanded) {
            actionSearch!!.collapseActionView()
        } else {
            super.onBackPressed();
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeSubscription.clear()
    }

}
