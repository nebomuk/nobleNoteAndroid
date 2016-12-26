package com.taiko.noblenote

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import rx.Observable
import rx.lang.kotlin.PublishSubject
import rx.subjects.PublishSubject

/***
 * contextual action mode for files
 */
class FileActionMode : ActionMode.Callback2()

{
    private  val mDestroySubject : PublishSubject<Unit> = PublishSubject()
    private var mMenu : Menu? = null

    val onDestroy : Observable<Unit>
    get() = mDestroySubject.take(1)

    override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
        return false;
    }

    override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
        return true;
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {

        val inflater = mode.getMenuInflater()
        inflater.inflate(R.menu.note_list_cab, menu)
        mMenu = menu

        return true;
    }

    override fun onDestroyActionMode(p0: ActionMode?) {
        mDestroySubject.onNext(Unit)
    }


}