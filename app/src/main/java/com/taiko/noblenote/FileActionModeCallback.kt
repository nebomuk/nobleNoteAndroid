package com.taiko.noblenote

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import rx.lang.kotlin.PublishSubject
import rx.subjects.PublishSubject

/***
 * contextual action mode for files
 */
class FileActionModeCallback : ActionMode.Callback

{
    val onDestroy : PublishSubject<Unit> = PublishSubject()

     val onRemove : PublishSubject<Unit> = PublishSubject()
    val onRename : PublishSubject<Unit> = PublishSubject()
    val onShowHtml : PublishSubject<Unit> = PublishSubject()

    private var mMenu : Menu? = null


    override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
        return false;
    }

    override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
        when(p1?.itemId)
        {
            R.id.actionRemove -> onRemove.onNext(Unit)
            R.id.actionRename -> onRename.onNext(Unit)
            R.id.actionShowHtml -> onShowHtml.onNext(Unit)
        }
        return true;
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {

        val inflater = mode.getMenuInflater()
        inflater.inflate(R.menu.note_list_cab, menu)
        mMenu = menu

        return true;
    }

    override fun onDestroyActionMode(p0: ActionMode?) {
        onDestroy.onNext(Unit)
    }


}