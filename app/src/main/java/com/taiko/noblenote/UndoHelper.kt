package com.taiko.noblenote

import android.support.design.widget.Snackbar
import android.view.View
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.toObservable
import rx.schedulers.Schedulers
import java.io.File

/**
 * file deletion undo helper
 */
class UndoHelper {


    fun remove(files : List<File>, snackbarRootView : View)
    {
        val tempDir = createTempDir();
        // todo use rename instead of copy
        val copyToTempSub = files.toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.isDirectory) {
                        FileHelper.directoryMove(it,File(tempDir,it.name))
                    }
                }
                .subscribe(
                        {}, // onNext
                        {}, // onError
                        { // onCompleted
                    Snackbar.make(snackbarRootView,"items removed", Snackbar.LENGTH_LONG)
                            .setAction("undo",{})
                            .addCallback(object : Snackbar.Callback()
                    {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if(event != DISMISS_EVENT_ACTION) {
                                Observable.just(Unit).subscribeOn(Schedulers.io()).subscribe { tempDir.deleteRecursively() }
                            }
                            else
                            {

                            }
                        }

                    }).show();
                })

    }


}