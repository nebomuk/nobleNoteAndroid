package de.blogspot.noblenoteandroid

import android.os.Handler
import android.os.Looper
import android.view.ActionMode
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import de.blogspot.noblenoteandroid.R
import com.google.android.material.snackbar.Snackbar
import de.blogspot.noblenoteandroid.adapters.RecyclerFileAdapter
import de.blogspot.noblenoteandroid.filesystem.SFile
import de.blogspot.noblenoteandroid.filesystem.UndoHelper
import de.blogspot.noblenoteandroid.util.loggerFor
import rx.Observable
import rx.lang.kotlin.PublishSubject
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import java.util.*

/***
 * handles file list selection and contextual toolbar actions
 */
class ListSelectionController(private val view : View, private val adapter: RecyclerFileAdapter, val toolbar : Toolbar)
{

    private val log = loggerFor()

    var isTwoPane: Boolean = false; // folder list in two pane, colors each list item when clicked
    var isNoteList = false // show html source and cut action

    val showHtml = PublishSubject<SFile>();

    val fabVisible = PublishSubject<Int>();


    private var mActionMode : ActionMode? = null

    private val mFileActionModeCallback = FileActionModeCallback(view.context);

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
                Dialogs.showRenameDialog(view, selectedFile, onRenamed = {
                    adapter.refresh()
                    mActionMode?.finish();
                }, onNotRenamed = { mActionMode?.finish() })
            }

        }

        mCompositeDisposable += mFileActionModeCallback.onShowHtml.subscribe {

            val selectedFile = adapter.selectedFiles.firstOrNull()
            if (selectedFile != null) {
                showHtml.onNext(selectedFile);
                mActionMode?.finish()
            }
        }

        // remove files from the fs with undo snackbar
        mCompositeDisposable += mFileActionModeCallback.onRemove.subscribe {
            val selectedFiles = ArrayList<SFile>(adapter.selectedFiles.map { it  }) // shallow copy
            UndoHelper.remove(selectedFiles,view, onRemovedOrUndo = {adapter.refresh()})
            mActionMode?.finish()
        }

        mCompositeDisposable += mFileActionModeCallback.onCut.subscribe {
            FileClipboard.cutFiles(adapter.selectedFiles.map { it }) // warning: Singleton causes memory leak when listener not disposed
            Snackbar.make(view,view.context.getString(R.string.msg_n_notes_in_clipboard,adapter.selectedFiles.size), Snackbar.LENGTH_SHORT).show();
            adapter.clearSelection();
            mActionMode?.finish()
        }

        mCompositeDisposable += mFileActionModeCallback.onDestroy.subscribe {
            adapter.clearSelection()
            mActionMode = null
            if(isTwoPane) {
                adapter.selectFolderOnClick = true;
            }
            fabVisible.onNext(View.VISIBLE);
        }

        mCompositeDisposable += adapter.itemLongClicks().subscribe {

            mActionMode = toolbar.startActionMode(mFileActionModeCallback);
            adapter.selectFolderOnClick = false;
            mActionMode?.menu?.findItem(R.id.actionShowHtml)?.isVisible = isNoteList
            mActionMode?.menu?.findItem(R.id.actionCut)?.isVisible = isNoteList


            adapter.setSelected(it,true);
            fabVisible.onNext(View.INVISIBLE);
        }


        mCompositeDisposable += adapter.itemClicks()
                .doOnNext { log.i("item click: " + it) }
                .subscribe {

            if(mActionMode != null)
            {

                toggleSelection(it)

                val count = adapter.selectedFiles.size

                mActionMode?.menu?.findItem(R.id.actionCut)?.isVisible =  isNoteList
                mActionMode?.menu?.findItem(R.id.actionShowHtml)?.isVisible = (count == 1 && isNoteList == true);
                mActionMode?.menu?.findItem(R.id.actionRename)?.isVisible = count == 1;
                mActionMode?.customView?.findViewById<TextView>(R.id.item_count)?.text = if(count > 0) count.toString() else "";

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