package com.taiko.noblenote

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.github.developerpaul123.filepickerlibrary.FilePickerActivity
import com.github.developerpaul123.filepickerlibrary.enums.Request
import com.github.developerpaul123.filepickerlibrary.enums.ThemeType
import com.jakewharton.rxbinding.view.clicks
import com.tbruyelle.rxpermissions.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_twopane.*
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
            Pref.selectedFolderPath = path;

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


    // https://stackoverflow.com/questions/26600263/how-do-i-prevent-the-status-bar-and-navigation-bar-from-animating-during-an-acti
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {
            val fade = Fade()
            fade.excludeTarget(android.R.id.statusBarBackground, true)
            fade.excludeTarget(android.R.id.navigationBarBackground, true)
            window.exitTransition = fade
            window.enterTransition = fade
        }

        mTwoPane = findViewById(R.id.item_detail_container) != null

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
        compositeSubscription += app.uiCommunicator.fileSelected.mergeWith(app.uiCommunicator.fileCreated).subscribe { NoteListFragment.startNoteEditor(this,it) }

        if(mTwoPane)
        {
            compositeSubscription += fab_menu_note.clicks().subscribe {
                Dialogs.showNewNoteDialog(this) {app.uiCommunicator.fileCreated.onNext(it)}
            }

            compositeSubscription += fab_menu_folder.clicks().subscribe {
                Dialogs.showNewFolderDialog(this,{app.uiCommunicator.folderCreated.onNext(it)})
            }
        }
        else
        {
            compositeSubscription += fab.clicks().subscribe {
                if(fragmentManager.backStackEntryCount > 0)
                {
                    Dialogs.showNewNoteDialog(this, {app.uiCommunicator.fileCreated.onNext(it)})
                }
                else
                {
                    Dialogs.showNewFolderDialog(this, {app.uiCommunicator.folderCreated.onNext(it)})
                }
            }
        }

        toolbar.inflateMenu(R.menu.menu_main)
        createOptionsMenu(toolbar.menu) // TODO fix crash
        toolbar.setOnMenuItemClickListener {

            val item = it
            // open file picker activity
            val itemId = item?.itemId

            // show directory chooser dialog
            if(itemId == R.id.action_rootPath)
            {
                val filePickerDialogIntent = Intent(this, FilePickerActivity::class.java)
                filePickerDialogIntent.putExtra(FilePickerActivity.THEME_TYPE, ThemeType.ACTIVITY);
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
            true;
        }
    }

    private var actionSearch: MenuItem? = null


    private fun createOptionsMenu(menu: Menu?) {

        actionSearch = menu?.findItem(R.id.action_search)

        search_view.setMenuItem(actionSearch)

//        val searchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
//        searchAutoComplete.threshold = 2
        val queryTextObs : Observable<CharSequence> = search_view.queryTextChanges()
                .share()
                .throttleWithTimeout(300, TimeUnit.MILLISECONDS, Schedulers.io())
                .filter { !it.isEmpty() }
                .distinctUntilChanged()
        val adapter = SuggestionAdapter(this, queryTextObs);
        search_view.setAdapter(adapter)
        search_view.setOnItemClickListener { adapterView, view, i, l ->
            val item = adapterView.adapter.getItem(i) as File;
            NoteListFragment.startNoteEditor(this,item);
        }
//        // requires some manifest stuff https://stackoverflow.com/questions/27378981/how-to-use-searchview-in-toolbar-android
//        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
    }

    override fun onBackPressed() {
//         open close search view

        // close search
        if (search_view.isSearchOpen) {
            search_view.closeSearch()
        } else {
            super.onBackPressed();
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeSubscription.clear()
    }

}
