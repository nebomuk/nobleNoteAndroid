package com.taiko.noblenote

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.transition.Fade
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jakewharton.rxbinding.view.clicks
import com.taiko.noblenote.document.SFile
import com.taiko.noblenote.document.VolumeUtil
import com.taiko.noblenote.editor.EditorActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_twopane.fab_menu
import kotlinx.android.synthetic.main.activity_main_twopane.fab_menu_folder
import kotlinx.android.synthetic.main.activity_main_twopane.fab_menu_note
import kotlinx.android.synthetic.main.toolbar.*
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import rx.subscriptions.Subscriptions


class MainActivity : AppCompatActivity()
{


    var twoPane: Boolean = false

    private val mCompositeSubscription = CompositeSubscription()

    private var mVolumeSubscription  = Subscriptions.empty()
    private var mMainToolbarController: MainToolbarController? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null) // do not save instance state because we create fragments manually with updates filesystem state
        log.d(".onCreate()");

        setContentView(R.layout.activity_main)

        // https://stackoverflow.com/questions/26600263/how-do-i-prevent-the-status-bar-and-navigation-bar-from-animating-during-an-acti
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {
            val fade = Fade()
            fade.excludeTarget(android.R.id.statusBarBackground, true)
            fade.excludeTarget(android.R.id.navigationBarBackground, true)
            window.exitTransition = fade
            window.enterTransition = fade
        }

        twoPane = findViewById<View>(R.id.item_detail_container) != null // two pane uses the refs.xml reference to reference activity_main_twopane.xml as activity_main.xml

        toolbar.inflateMenu(R.menu.menu_main)

        val dlg = VolumeNotAccessibleDialog.create(this);

        lifecycle.addObserver(VolumeUtil);

        LegacyStorageMigration.migrateFromLegacyStorage(this);

        mVolumeSubscription = VolumeUtil.volumeAccessibleObservable(this, Pref.rootPath)

                .subscribe {
                    if(it)
                    {
                        if(dlg.isShowing)
                        {
                            dlg.dismiss();
                        }
                        attachUi();
                    }
                    else
                    {
                        dlg.show();
                        detachUi()
                    }
                     }
    }


    private fun attachUi()
    {
        log.d(".setupUi()");

        detachUi();
        // replaces existing fragments that have been retaind in saveInstanceState
        supportFragmentManager.beginTransaction().replace(R.id.item_master_container, FolderListFragment()).commitAllowingStateLoss()

        val app = application as MainApplication
        mCompositeSubscription += app.eventBus.fileSelected.mergeWith(app.eventBus.createFileClick).subscribe { startNoteEditor(this,it, EditorActivity.READ_WRITE) }



        if(twoPane)
        {
            fab_menu.setClosedOnTouchOutside(true)

            mCompositeSubscription += fab_menu_note.clicks().subscribe {
                Dialogs.showNewNoteDialog(this.coordinator_layout) {app.eventBus.createFileClick.onNext(it)}
                fab_menu.close(true);
            }

            mCompositeSubscription += fab_menu_folder.clicks().subscribe {
                Dialogs.showNewFolderDialog(this.coordinator_layout,{app.eventBus.createFolderClick.onNext(it)})
                fab_menu.close(true);
            }
        }
        else
        {
            mCompositeSubscription += fab.clicks().subscribe {
                if(supportFragmentManager.backStackEntryCount > 0)
                {
                    Dialogs.showNewNoteDialog(coordinator_layout, {app.eventBus.createFileClick.onNext(it)})
                }
                else
                {
                    Dialogs.showNewFolderDialog(coordinator_layout, {app.eventBus.createFolderClick.onNext(it)})
                }
            }
        }

        mMainToolbarController = MainToolbarController(this)

        val handler = Handler();

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener {
            handler.postDelayed({swipeRefreshLayout.isRefreshing = false},500)
            app.eventBus.swipeRefresh.onNext(Unit)
            log.v("SwipeToRefresh");
        }

        mCompositeSubscription += Subscriptions.create { swipeRefreshLayout.setOnRefreshListener(null);  }

    }

    override fun onBackPressed() {

        // close search
        if(mMainToolbarController?.onBackPressed() == false)
        {
            super.onBackPressed();
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        log.d(".onDestroy()");
        detachUi()
        mVolumeSubscription.unsubscribe();
    }

    private fun detachUi() {
        mCompositeSubscription.clear()
        mMainToolbarController?.clearSubscriptions();
        mMainToolbarController = null;

        for (fragment in supportFragmentManager.fragments) {
            supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
    }

    /**
     * sets the visibility of the floating action button
     */
    fun setFabVisible(b : Boolean)
    {
        if(b)
        {
            this.fab?.visibility = View.VISIBLE;
            this.fab_menu?.visibility = View.VISIBLE;
        }
        else
        {
            this.fab?.visibility = View.INVISIBLE;
            this.fab_menu?.close(false);
            this.fab_menu?.visibility = View.INVISIBLE;
        }
    }

    companion object {
        private val log = loggerFor();


        @JvmStatic
                // start the note editor
        fun startNoteEditor(activity : Context, file: SFile, argOpenMode : String, argQueryText : String = "") {

            val intent = Intent(activity, EditorActivity::class.java)
            intent.putExtra(EditorActivity.ARG_NOTE_URI, file.uri.toString())
            intent.putExtra(EditorActivity.ARG_OPEN_MODE, argOpenMode)
            intent.putExtra(EditorActivity.ARG_QUERY_TEXT,argQueryText);
            activity.startActivity(intent);
        }

    }



}
