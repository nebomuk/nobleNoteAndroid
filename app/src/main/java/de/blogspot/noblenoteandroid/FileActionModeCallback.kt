package de.blogspot.noblenoteandroid

import android.content.Context
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import de.blogspot.noblenoteandroid.R
import rx.lang.kotlin.PublishSubject
import rx.subjects.PublishSubject

/***
 * contextual action mode for files
 *
 * just an rx java adapter, most stuff is done in ListSelectionController
 */
class FileActionModeCallback(val mContext : Context) : ActionMode.Callback

{
    val onDestroy : PublishSubject<Unit> = PublishSubject()

     val onRemove : PublishSubject<Unit> = PublishSubject()
    val onRename : PublishSubject<Unit> = PublishSubject()
    val onShowHtml : PublishSubject<Unit> = PublishSubject()
    val onCut : PublishSubject<Unit> = PublishSubject()

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
            R.id.actionCut -> onCut.onNext(Unit);
        }
        return true;
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {


        val inflater = mode.menuInflater
        inflater.inflate(R.menu.note_list_cab, menu)
        mMenu = menu
        val actionModeView = LayoutInflater.from(mContext).inflate(R.layout.actionmode,null);
        mode.customView = actionModeView;

        return true;
    }

    override fun onDestroyActionMode(p0: ActionMode?) {
        onDestroy.onNext(Unit)
    }


}