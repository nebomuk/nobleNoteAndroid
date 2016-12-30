package com.taiko.noblenote

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.ActionMode
import rx.Observable

/***
 * handles list selection and contextual toolbar actions
 */
class ListController(val activity: MainActivity,  val recyclerView: RecyclerView)
{
    var mActionMode : ActionMode? = null
    val adapter : RecyclerFileAdapter = recyclerView.adapter as RecyclerFileAdapter;
    val mFileActionModeCallback = FileActionModeCallback()

    /**
     * item clicks when action mode is not active
     */
    fun itemClicks() : Observable<Int>
    {
        return adapter.itemClicks().filter { mActionMode == null }
    }

    init {

        val handler = Handler(Looper.getMainLooper())

        mFileActionModeCallback.onRemove.subscribe { adapter.removeSelected() }

        mFileActionModeCallback.onDestroy.subscribe {
            adapter.clearSelection()
            mActionMode = null
            activity.setFabVisible(true);
        }

        adapter.itemLongClicks().subscribe {

            mActionMode = activity.startActionMode(mFileActionModeCallback);
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

                mActionMode?.menu?.findItem(R.id.actionShowHtml)?.isVisible = count == 1;
                mActionMode?.menu?.findItem(R.id.actionRename)?.isVisible = count == 1;

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