package com.taiko.noblenote

import android.Manifest
import android.app.Activity
import android.app.FragmentManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import com.github.developerpaul123.filepickerlibrary.FilePickerActivity
import com.github.developerpaul123.filepickerlibrary.enums.Request
import com.github.developerpaul123.filepickerlibrary.enums.ThemeType
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.tbruyelle.rxpermissions.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import rx.Observable
import rx.lang.kotlin.plusAssign
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import rx.subscriptions.Subscriptions
import rx_activity_result.RxActivityResult
import java.io.File
import java.util.concurrent.TimeUnit

/***
 * controller class for the MainActivity
 */
class MainToolbarController(val mainActivity: MainActivity)
{
    private val mCompositeSubscription : CompositeSubscription = CompositeSubscription();

    init {

        mainActivity.toolbar.inflateMenu(R.menu.menu_main)
        mainActivity.toolbar.setOnMenuItemClickListener {

            val item = it
            // open file picker activity
            val itemId = item?.itemId

            // show directory chooser dialog
            if(itemId == R.id.action_rootPath)
            {
                startFolderPicker()
            }
            true;
        }

        mCompositeSubscription.add(Subscriptions.create {
            mainActivity.toolbar.setOnMenuItemClickListener(null);
            mainActivity.search_view.setOnSearchViewListener(null);

        })

        initSearch(mainActivity.toolbar.menu);


    }

    private fun startFolderPicker()
    {
        val filePickerDialogIntent = Intent(mainActivity, FilePickerActivity::class.java)
        filePickerDialogIntent.putExtra(FilePickerActivity.THEME_TYPE, ThemeType.ACTIVITY);
        filePickerDialogIntent.putExtra(FilePickerActivity.REQUEST, Request.DIRECTORY);

        mCompositeSubscription += Observable.concat(RxPermissions(mainActivity).request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .map {
                    if (it) RxActivityResult.on(mainActivity).startIntent(filePickerDialogIntent) else Observable.empty()
                }).subscribe {

            if ((it.resultCode() == Activity.RESULT_OK)) {
                val path = it.data().getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH)
                Pref.rootPath.onNext(path)
                Toast.makeText(mainActivity, mainActivity.getString(R.string.msg_directory_selected) + " " + Pref.rootPath.value, Toast.LENGTH_LONG).show();

            }
        }
    }


    private lateinit var mSearchAdapter: SuggestionAdapter

    private fun initSearch(menu: Menu?) {

        val actionSearch = menu?.findItem(R.id.action_search)

        mainActivity.search_view.setMenuItem(actionSearch)


        mainActivity.search_view.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener
        {
            override fun onSearchViewShown() {
                mainActivity.setFabVisible(false);
            }

            override fun onSearchViewClosed() {
                mainActivity.setFabVisible(true);

            }

        }
        )

//        val searchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
//        searchAutoComplete.threshold = 2


        val queryTextObs = mainActivity.search_view.queryTextChanges().share();

        mCompositeSubscription += queryTextObs.filter { it.isSubmit }.subscribe {
            showSearchResults(it.text);
        }

        val suggestions = queryTextObs.filter { !it.isSubmit && !it.text.isNullOrBlank() }
        .map { it.text }
        .throttleWithTimeout(200, TimeUnit.MILLISECONDS, Schedulers.io())
        .distinctUntilChanged();

        mSearchAdapter = SuggestionAdapter(mainActivity, suggestions);
        mainActivity.search_view.setAdapter(mSearchAdapter)
        mainActivity.search_view.setOnItemClickListener { adapterView, view, i, l ->
            val item = adapterView.adapter.getItem(i) as File;
            NoteListFragment.startNoteEditor(mainActivity,item, NoteEditorActivity.READ_WRITE,mainActivity.search_view.queryText.toString());
            mainActivity.search_view.closeSearch();
        }
//        // requires some manifest stuff https://stackoverflow.com/questions/27378981/how-to-use-searchview-in-toolbar-android
//        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
    }


    private fun showSearchResults(queryText : CharSequence)
    {
        mainActivity.fragmentManager.popBackStack(FRAGMENT_SEARCH_RESULT, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        mainActivity.search_view.clearFocus();
        mSearchAdapter.clear();
        mainActivity.setFabVisible(false);

        val frag = NoteListFragment()
        val arguments = Bundle()
        arguments.putString(NoteListFragment.ARG_QUERY_TEXT,queryText.toString())
        frag.arguments = arguments;
        mainActivity.fragmentManager.beginTransaction().add(R.id.item_master_container,frag).addToBackStack(FRAGMENT_SEARCH_RESULT).commit();

    }

    companion object
    {
        @JvmStatic
        val FRAGMENT_SEARCH_RESULT = "fragment_search_result";
    }

    /**
     * @return true when handled, false when super class should be called
     */
    fun onBackPressed() : Boolean
    {
        val handled = mainActivity.search_view.isSearchOpen;

        if (handled) {
            mainActivity.search_view.closeSearch();
            mainActivity.fragmentManager.popBackStack(FRAGMENT_SEARCH_RESULT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mainActivity.setFabVisible(true);
        }
        return handled;
    }

    fun destroy()
    {
        mCompositeSubscription.clear();
    }
}