package com.taiko.noblenote

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.transition.Fade
import android.view.View
import com.jakewharton.rxbinding.view.clicks
import com.tbruyelle.rxpermissions.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_twopane.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription


class MainActivity : Activity()
{

    private var mTwoPane: Boolean = false

    val compositeSubscription = CompositeSubscription()
    lateinit var mainToolbarController : MainToolbarController


    public companion object {
        @JvmField val ARG_TWO_PANE = "two_pane"
    }

    public fun onFolderSelected(path: String) {

            val arguments = Bundle()
            arguments.putString(NoteListFragment.ARG_FOLDER_PATH, path)
            arguments.putBoolean(MainActivity.ARG_TWO_PANE, mTwoPane)
            val fragment = NoteListFragment()
            fragment.arguments = arguments
            Pref.currentFolderPath.onNext(path);

        if (mTwoPane) {
            fragmentManager.beginTransaction().replace(R.id.item_detail_container, fragment).commit()
        } else {
            fragmentManager.beginTransaction().add(R.id.item_master_container, fragment).addToBackStack("").commit();
        }
    }

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
                val folderFrag = FolderListFragment()
                fragmentManager.beginTransaction().add(R.id.item_master_container, folderFrag).commit()

            }
        }

        val app = application as MainApplication
        compositeSubscription += app.uiCommunicator.folderSelected.subscribe { onFolderSelected(it.absolutePath) }
        compositeSubscription += app.uiCommunicator.fileSelected.mergeWith(app.uiCommunicator.createFileClick).subscribe { NoteListFragment.startNoteEditor(this,it) }

        if(mTwoPane)
        {
            compositeSubscription += fab_menu_note.clicks().subscribe {
                Dialogs.showNewNoteDialog(this) {app.uiCommunicator.createFileClick.onNext(it)}
            }

            compositeSubscription += fab_menu_folder.clicks().subscribe {
                Dialogs.showNewFolderDialog(this,{app.uiCommunicator.createFolderClick.onNext(it)})
            }
        }
        else
        {
            compositeSubscription += fab.clicks().subscribe {
                if(fragmentManager.backStackEntryCount > 0)
                {
                    Dialogs.showNewNoteDialog(this, {app.uiCommunicator.createFileClick.onNext(it)})
                }
                else
                {
                    Dialogs.showNewFolderDialog(this, {app.uiCommunicator.createFolderClick.onNext(it)})
                }
            }
        }

        mainToolbarController = MainToolbarController(this)
    }

    override fun onBackPressed() {
        // close search
        if(!mainToolbarController.onBackPressed())
        {
            super.onBackPressed();
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeSubscription.clear()
    }

    fun setFabVisible(b : Boolean)
    {
        if(b)
        {
            this.fab?.visibility = View.VISIBLE;
            this.fab_menu_folder?.visibility = View.VISIBLE;
            this.fab_menu_note?.visibility = View.VISIBLE;
        }
        else
        {
            this.fab?.visibility = View.INVISIBLE;
            this.fab_menu_folder?.visibility = View.INVISIBLE;
            this.fab_menu_note?.visibility = View.INVISIBLE;
        }
    }

}
