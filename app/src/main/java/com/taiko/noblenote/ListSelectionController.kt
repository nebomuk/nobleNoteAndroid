package com.taiko.noblenote

import android.os.Handler
import android.os.Looper
import com.google.android.material.snackbar.Snackbar
import android.view.ActionMode
import kotlinx.android.synthetic.main.actionmode.view.*
import kotlinx.android.synthetic.main.activity_main.*
import rx.Observable
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import java.io.File
import java.util.*

/***
 * handles file list selection and contextual toolbar actions
 */
class ListSelectionController(private val activity: MainActivity, private val adapter: RecyclerFileAdapter)
{
    private val log = loggerFor()

    var isTwoPane: Boolean = false; // folder list in two pane, colors each list item when clicked
    var isHtmlActionAvailable = false // show html source action


    private var mActionMode : ActionMode? = null

    private val mFileActionModeCallback = FileActionModeCallback(activity);

    private val  mCompositeDisposable : CompositeSubscription = CompositeSubscription()

    /**
     * item clicks when action mode is not active
     *
     * used by outer classes
     */
    fun itemClicks() : Observable<Int>
    {
        return adapter.itemClicks().filter { mActionMode == null }
    }

    init {

        val handler = Handler(Looper.getMainLooper())

        mCompositeDisposable += mFileActionModeCallback.onRename.subscribe {
            val selectedFile = adapter.selectedFiles.firstOrNull()
            if (selectedFile != null) {
                Dialogs.showRenameDialog(activity,activity.coordinator_layout, selectedFile, onRenamed = {
                    adapter.removeSelected();
                    adapter.addFileName(it.name);
                    mActionMode?.finish();
                },
                        onNotRenamed = { mActionMode?.finish() })
            }

        }

        mCompositeDisposable += mFileActionModeCallback.onShowHtml.subscribe {

            val selectedFile = adapter.selectedFiles.firstOrNull()
            if (selectedFile != null) {
                MainActivity.startNoteEditor(activity,selectedFile, EditorActivity.HTML)
                mActionMode?.finish()
            }
        }

        // remove files from the fs with undo snackbar
        mCompositeDisposable += mFileActionModeCallback.onRemove.subscribe {
            val selectedFiles = ArrayList<SFile>(adapter.selectedFiles.map { it  }) // shallow copy
            UndoHelper.remove(selectedFiles,activity.coordinator_layout,  onUndo = {selectedFiles.forEach { adapter.addFileName(it.name) }})
            adapter.removeSelected()
            mActionMode?.finish()
        }

        mCompositeDisposable += mFileActionModeCallback.onCut.subscribe {
            FileClipboard.cutFiles(adapter.selectedFiles.map { it }) // warning: Singleton causes memory leak when listener not disposed
            Snackbar.make(activity.coordinator_layout,activity.getString(R.string.msg_n_notes_in_clipboard,adapter.selectedFiles.size), Snackbar.LENGTH_SHORT).show();
            adapter.clearSelection();
            mActionMode?.finish()
        }

        mCompositeDisposable += mFileActionModeCallback.onDestroy.subscribe {
            adapter.clearSelection()
            mActionMode = null
            if(isTwoPane) {
                adapter.selectFolderOnClick = true;
            }
            activity.setFabVisible(true);
        }

        mCompositeDisposable += adapter.itemLongClicks().subscribe {

            mActionMode = activity.startActionMode(mFileActionModeCallback);
            adapter.selectFolderOnClick = false;
            mActionMode?.menu?.findItem(R.id.actionShowHtml)?.isVisible = isHtmlActionAvailable

            adapter.setSelected(it,true);
            activity.setFabVisible(false);
        }


        mCompositeDisposable += adapter.itemClicks()
                .doOnNext { log.i("item click: " + it) }
                .subscribe {

            if(mActionMode != null)
            {

                toggleSelection(it)

                val count = adapter.selectedFiles.size

                mActionMode?.menu?.findItem(R.id.actionCut)?.isVisible = count > 1
                mActionMode?.menu?.findItem(R.id.actionShowHtml)?.isVisible = (count == 1 && isHtmlActionAvailable == true);
                mActionMode?.menu?.findItem(R.id.actionRename)?.isVisible = count == 1;
                mActionMode?.customView?.item_count?.text = if(count > 0) count.toString() else "";

                if(count == 0) {
                    // avoid itemClick raze hazard
                    handler.post {
                        mActionMode?.finish();
                        mActionMode = null;
                    }


                }

            }
        }
    }

    private  fun toggleSelection(pos : Int)
    {
        adapter.setSelected(pos = pos,isSelected = !adapter.isSelected(pos = pos))
    }

    fun clearSubscriptions()
    {
        mCompositeDisposable.clear()
    }

}