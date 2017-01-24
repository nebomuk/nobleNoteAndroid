package com.taiko.noblenote

import rx.Observable
import rx.lang.kotlin.toObservable
import rx.lang.kotlin.toSingletonObservable
import java.io.File

/**
 * full text search inside files and subdirs
 */
object FindInFiles {

    /**
     * @return the file paths of the files
     */
    @JvmStatic
    fun  findHtmlInFiles(path : String, queryText: CharSequence) : Observable<String> =
            File(path).walkBottomUp().filter { it.isFile && !it.isHidden }
                    .toObservable()
                    .map {
                        val filePath = it.path;
                        //                Timber.i(it.name)
                        if (it.name.contains(queryText, true)) {
                            filePath.toSingletonObservable() // file name contains the queryText, return the filePath
                        } else {
                            Observable.using(// open the file and try to find the queryText inside
                                    { it.bufferedReader() },
                                    { it.lineSequence().toObservable() },
                                    { it.close() })
                                    .exists { it.contains(queryText, true) }
                                    .filter { it == true } // found
                                    .map { filePath }
                        }
                    }
                    .concat()


    fun <T> Observable<Observable<T>>.concat(): Observable<T> = Observable.concat(this)
}