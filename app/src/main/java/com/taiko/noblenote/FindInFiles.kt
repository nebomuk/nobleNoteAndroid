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
    fun  findHtmlInFiles(file: File, queryText: CharSequence) : Observable<String> =
            file.walkBottomUp().filter { it.isFile && !it.isHidden }
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


    // assumes the regular folder structure without subfolders except for root for performance
    @JvmStatic
    fun findHtmlInFiles(directoryToSearch : SFile, queryText: CharSequence) : Observable<SFile> {

        // work on DocumentFile directly to increase performance
        return directoryToSearch.doc.listFiles().toObservable().flatMap {
            it.listFiles().toObservable()
        }
                .map { note ->
                    if (note.name.orEmpty().contains(queryText, true)) {
                        note.toSingletonObservable() // file name contains the queryText, return the filePath
                    } else {
                        Observable.using(// open the file and try to find the queryText inside
                                { note.openInputStream().bufferedReader() },
                                { it.lineSequence().toObservable() },
                                { it.close() })
                                .exists { it.contains(queryText, true) }
                                .filter { it == true } // found
                                .map { note }
                    }
                }
                .concat()
                .map { SFile(it) }


    }
}