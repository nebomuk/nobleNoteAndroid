package com.taiko.noblenote

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.ActionMode
import rx.Observable


class ListController(val activity: Activity,  val recyclerView: RecyclerView)
{
    var mActionMode : ActionMode? = null
    val adapter : RecyclerFileAdapter;

    /**
     * item clicks when action mode is not active
     */
    fun itemClicks() : Observable<Int>
    {
        return adapter.itemClicks().filter { mActionMode == null }
    }

    init {

        val handler = Handler(Looper.getMainLooper())
        adapter = recyclerView.adapter as RecyclerFileAdapter

        adapter.itemLongClicks().subscribe {
            val fileActionMode = FileActionMode()
            fileActionMode.onDestroy.subscribe {
                adapter.clearSelection()
                mActionMode = null
            }

            mActionMode = activity.startActionMode(fileActionMode)
                adapter.setSelected(it,true);
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