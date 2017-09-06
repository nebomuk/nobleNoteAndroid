package com.taiko.noblenote

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.ActionMode
import kotlinx.android.synthetic.main.actionmode.view.*
import rx.Observable
import java.io.File
import java.util.*

/***
 * handles file list selection and contextual toolbar actions
 */
class ListSelectionController(val activity: MainActivity, val recyclerView: RecyclerView)
{
    var isTwoPane: Boolean = false; // folder list in two pane, colors each list item when clicked
    var isHtmlActionAvailable = false // show html source action


    private var mActionMode : ActionMode? = null
    private val adapter : RecyclerFileAdapter = recyclerView.adapter as RecyclerFileAdapter;

    private val mFileActionModeCallback = FileActionModeCallback(activity);

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

        mFileActionModeCallback.onRename.subscribe {
            val selectedFile = adapter.selectedFiles.firstOrNull()
            if (selectedFile != null) {
                Dialogs.showRenameDialog(activity,recyclerView, selectedFile, onRenamed = {
                    adapter.removeSelected();
                    adapter.addFile(it);
                    mActionMode?.finish();
                },
                        onNotRenamed = { mActionMode?.finish() })
            }

        }

        mFileActionModeCallback.onShowHtml.subscribe {

            val selectedFile = adapter.selectedFiles.firstOrNull()
            if (selectedFile != null) {
                NoteListFragment.startNoteEditor(activity,selectedFile,NoteEditorActivity.HTML)
                mActionMode?.finish()
            }
        }

        // remove files from the fs with undo snackbar
        mFileActionModeCallback.onRemove.subscribe {
            val selectedFiles = ArrayList<File>(adapter.selectedFiles) // shallow copy
            UndoHelper.remove(selectedFiles,recyclerView,  onUndo = {selectedFiles.forEach { adapter.addFile(it) }})
            adapter.removeSelected()
            mActionMode?.finish()
        }

        mFileActionModeCallback.onDestroy.subscribe {
            adapter.clearSelection()
            mActionMode = null
            if(isTwoPane) {
                adapter.selectFolderOnClick = true;
            }
            activity.setFabVisible(true);
        }

        adapter.itemLongClicks().subscribe {

            mActionMode = activity.startActionMode(mFileActionModeCallback);
            adapter.selectFolderOnClick = false;
            mActionMode?.menu?.findItem(R.id.actionShowHtml)?.isVisible = isHtmlActionAvailable
            adapter.setSelected(it,true);
            activity.setFabVisible(false);
        }


        adapter.itemClicks()
                .doOnNext { KLog.i("item click: " + it) }
                .subscribe {

            if(mActionMode != null)
            {

                toggleSelection(it)

                val count = adapter.selectedFiles.size

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

}