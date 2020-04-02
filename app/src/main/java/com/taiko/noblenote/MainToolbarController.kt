package com.taiko.noblenote

import android.app.FragmentManager
import android.content.Intent
import android.database.DataSetObserver
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.view.MenuItem
import com.jakewharton.rxbinding.view.clicks
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.taiko.noblenote.document.SFile
import com.taiko.noblenote.editor.EditorActivity
import com.taiko.noblenote.extensions.queryText
import com.taiko.noblenote.extensions.queryTextChanges
import com.taiko.noblenote.preferences.PreferencesActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
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

    private lateinit var mSearchAdapter: SuggestionAdapter


    init {

        activity.toolbar.menu.findItem(R.id.action_settings)
                .setOnMenuItemClickListener{
                    activity.startActivity(Intent(activity, PreferencesActivity::class.java))
                    true
                }

        var pasteFileDisposable = Subscriptions.empty()

        activity.supportFragmentManager.addOnBackStackChangedListener {

            val pasteFileMenuItem = activity.toolbar.menu.findItem(R.id.action_paste);

            log.v("BackstackEntryCount: ${activity.supportFragmentManager.backStackEntryCount}")

            if(activity.supportFragmentManager.backStackEntryCount > 0)
            {

                val fragment = activity.supportFragmentManager.findFragmentById(R.id.item_master_container);
                val folderPath = fragment?.arguments?.getString(NoteListFragment.ARG_FOLDER_PATH,null).orEmpty();
                activity.toolbar.title = SFile(folderPath).name;

                setBackNavigationIconEnabled(true)
                val instance = FileClipboard;
                pasteFileMenuItem.isEnabled = instance.hasContent;

                pasteFileDisposable.unsubscribe()

                log.v("addPasteFileListener folderPath: $folderPath");

                pasteFileDisposable = addPasteFileListener(pasteFileMenuItem,folderPath);

            }
            else
            {
                pasteFileDisposable.unsubscribe()
                setBackNavigationIconEnabled(false)
                pasteFileMenuItem.isEnabled = false;

                activity.toolbar.title = null;
            }
        }


        initSearch();
    }

    private fun addPasteFileListener(item : MenuItem, folderPath : String): Subscription {
        item.isEnabled = FileClipboard.hasContent && !folderPath.isEmpty();

        return item.clicks().subscribe {
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

        activity.search_view.setMenuItem(actionSearch)

        activity.search_view.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener
        {
            override fun onSearchViewShown() {
                activity.setFabVisible(false);
            }

            override fun onSearchViewClosed() {
                activity.setFabVisible(true);
            }
        }
        )

        mCompositeSubscription.add(Subscriptions.create {
            activity.search_view.setOnSearchViewListener(null);
        })

//        val searchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete
//        searchAutoComplete.threshold = 2

        val queryTextObs = activity.search_view.queryTextChanges().share();

        mCompositeSubscription += queryTextObs.filter { it.isSubmit }.subscribe {
            showSearchResults(it.text);
        }

        val suggestions = queryTextObs.filter { !it.isSubmit && !it.text.isNullOrBlank() }
        .map { it.text }
        .throttleWithTimeout(400, TimeUnit.MILLISECONDS, Schedulers.io())
        .distinctUntilChanged();

        mSearchAdapter = SuggestionAdapter(activity, suggestions);

        // workaround for the adapter's android.widget.Filter sometimes not calling onFilterCompleted in MaterialSearchView
        mSearchAdapter.registerDataSetObserver( object : DataSetObserver() {

            override fun onChanged() {
                if(mSearchAdapter.count > 0)
                {
                    activity.search_view.showSuggestions();
                }
            }
        })
        activity.search_view.setAdapter(mSearchAdapter)
        activity.search_view.setOnItemClickListener { adapterView, view, i, l ->
            val item = adapterView.adapter.getItem(i) as SFile;
            MainActivity.startNoteEditor(activity,item, EditorActivity.READ_WRITE, activity.search_view.queryText.toString());
            activity.search_view.closeSearch();
        }
//        // requires some manifest stuff https://stackoverflow.com/questions/27378981/how-to-use-searchview-in-toolbar-android
//        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
    }


    private fun showSearchResults(queryText : CharSequence)
    {
        activity.supportFragmentManager.popBackStack(FRAGMENT_SEARCH_RESULT, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        activity.search_view.clearFocus();
        mSearchAdapter.clear();
        activity.setFabVisible(false);

        val frag = NoteListFragment()
        val arguments = Bundle()
        arguments.putString(NoteListFragment.ARG_QUERY_TEXT,queryText.toString())
        frag.arguments = arguments;
        activity.supportFragmentManager.beginTransaction().add(R.id.item_master_container,frag).addToBackStack(FRAGMENT_SEARCH_RESULT).commit();

    }

    companion object
    {
        @JvmStatic
        val FRAGMENT_SEARCH_RESULT = "fragment_search_result";

        val log = loggerFor();
    }

    /**
     * @return true when handled, false when super class should be called
     */
    fun onBackPressed() : Boolean
    {
        val handled = activity.search_view.isSearchOpen;

        if (handled) {
            activity.search_view.closeSearch();
            activity.supportFragmentManager.popBackStack(FRAGMENT_SEARCH_RESULT, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            activity.setFabVisible(true);
        }
        return handled;
    }

    fun clearSubscriptions()
    {
        mCompositeSubscription.clear();
    }
}