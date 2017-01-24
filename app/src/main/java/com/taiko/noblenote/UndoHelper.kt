package com.taiko.noblenote

import android.os.Handler
import android.os.Looper
import android.support.design.widget.Snackbar
import android.view.View
import rx.Observable
import rx.lang.kotlin.toObservable
import rx.schedulers.Schedulers
import java.io.File

/**
 * file deletion undo helper
 */
object UndoHelper {

    val handler: Handler = Handler(Looper.getMainLooper())


    /**
     *  remove files from the fs with undo snackbar and a callback when the undo action has been executed
      */
    @JvmStatic
    fun remove(files: List<File>, snackbarRootView: View, onUndo: () -> Unit) {
        val tempDir = createTempDir(directory = File(Pref.rootPath.value))

        files.toObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        // onNext
                        {
                            if (it.isDirectory) {
                                FileHelper.directoryMove(it, File(tempDir, it.name))
                            } else if (it.isFile) {
                                FileHelper.fileMoveWithParent(it, tempDir)
                            }
                        },
                        {},
                        // onCompleted
                        {
                            handler.post {
                                Snackbar.make(snackbarRootView, R.string.msg_items_removed, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.action_undo, {})
                                        .addCallback(object : Snackbar.Callback() {
                                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                                if (event == DISMISS_EVENT_ACTION) // action undo
                                                {
                                                    KLog.i("Undo Removing files")
                                                    Observable.just(Unit)
                                                            .subscribeOn(Schedulers.io())
                                                            .subscribe(
                                                                    // onNext
                                                                    {
                                                                        // move the files back to its original destination
                                                                        FileHelper.directoryMove(tempDir, File(Pref.rootPath.value))
                                                                    },
                                                                    {},
                                                                    // onCompleted
                                                                    {
                                                                        // call undo callback
                                                                        handler.post {
                                                                            onUndo()
                                                                        }
                                                                    })
                                                }
                                                else
                                                {
                                                    KLog.i("Removing files")
                                                    // timeout/dimissed, remove the files permanently
                                                    Observable.just(Unit).subscribeOn(Schedulers.io()).subscribe {
                                                        tempDir.deleteRecursively()
                                                    }

                                                }
                                            }

                                        }).show();
                            }

                        })

    }


}