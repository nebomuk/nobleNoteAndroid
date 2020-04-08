package com.taiko.noblenote

import android.os.Handler
import android.os.Looper
import com.google.android.material.snackbar.Snackbar
import android.view.View
import com.taiko.noblenote.document.SFile
import rx.Observable
import rx.lang.kotlin.toObservable
import rx.schedulers.Schedulers
import java.io.File
import kotlin.random.Random

/**
 * file deletion undo helper
 */
object UndoHelper {

    val handler: Handler = Handler(Looper.getMainLooper())

    private  val log = loggerFor()

    /**
     *  remove files from the fs with undo snackbar and a callback when the undo action has been executed
      */
    @JvmStatic
    fun remove(files: List<SFile>, snackbarRootView: View, onUndo: () -> Unit) {

        if(files.isEmpty())
        {
            return;
        }

        val tempDir = SFile(SFile(Pref.rootPath.value.toString()), ".TempTrash"+ Random.nextInt(1,100));
        tempDir.mkdir()
        val originalFolder = files.first().parentFile;
        files.toObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        // onNext
                        {
                            val res = it.move(tempDir.uri);


                            if(!res)
                            {
                                log.d("Could not move $it to temporary directory for later removal");
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
                                                    log.i("Undo Removing files")
                                                    tempDir.listFiles().toObservable()
                                                            .subscribeOn(Schedulers.io())
                                                            .subscribe(
                                                                    // onNext
                                                                    {
                                                                        // move the files back to its original destination
                                                                        val b = it.move(originalFolder.uri);
                                                                        if(!b)
                                                                        {
                                                                            log.d("Could not move temporarily removed directory $tempDir back to original destination");
                                                                        }
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
                                                    log.i("Removing files")
                                                    // timeout/dimissed, remove the files permanently
                                                    Observable.just(Unit).subscribeOn(Schedulers.io()).subscribe {
                                                        val b = tempDir.deleteRecursively();
                                                        SFile.invalidateAllFileListCaches()
                                                        if(!b)
                                                        {
                                                            log.d("Could not remove all files in $tempDir");
                                                        }
                                                    }

                                                }
                                            }

                                        }).show();
                            }

                        })

    }


}