package com.taiko.noblenote

import android.net.Uri
import com.taiko.noblenote.document.IDocumentFile
import com.taiko.noblenote.document.SFile
import com.taiko.noblenote.document.openInputStream
import com.taiko.noblenote.extensions.MapWithIndex
import rx.Observable
import rx.lang.kotlin.toObservable
import rx.lang.kotlin.toSingletonObservable
import rx.schedulers.Schedulers
import java.io.File

/**
 * full text search inside files and subdirs
 */
object FindInFiles {

    private val log = loggerFor();

    @JvmStatic
    fun findInFiles(file: SFile, queryTextObservable : Observable<CharSequence>): Observable<MapWithIndex.Indexed<SFile>> {
        val obs = queryTextObservable
                .switchMap {

                    val queryText = it;
                    val searchRes = recursiveFullTextSearch(file, queryText);



                    searchRes.compose(MapWithIndex.instance())
                            .subscribeOn(Schedulers.io())
                            .concatWith(Observable.never()) // never complete
                }

        return obs;

    }

    /**
     * @return the file paths of the files
     */
    @JvmStatic
    private fun  recursiveFullTextSearch(file: File, queryText: CharSequence) : Observable<String> =
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


    private fun <T> Observable<Observable<T>>.concat(): Observable<T> = Observable.concat(this)


    // assumes the regular folder structure without subfolders except for root for performance
    @JvmStatic
    fun recursiveFullTextSearch(directoryToSearch : SFile, queryText: CharSequence) : Observable<SFile> {

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


    /**
     * queries usually start by typing Mon..,Monta..,Montana. If a lot of files do not contain Mon we do not need to search all these files again for Montana
     */
    private class FullTextSearchCache
    {
        fun returnDocumentIfItContains(doc : IDocumentFile, queryText: CharSequence) : Observable<IDocumentFile>
        {
            if(documentsWithoutQueryText.any { it == doc.uri })
            {
                return Observable.empty();
            }

            // FIXME can using replaced by useLines() ?

            return Observable.using(// open the file and try to find the queryText inside
                    { doc.openInputStream().bufferedReader() },
                    { it.lineSequence().toObservable() },
                    { it.close() })
                    .exists { it.contains(queryText, true) }
                    .doOnNext {
                        if(!it)
                        {
                            documentsWithoutQueryText.add(doc.uri);
                        }
                    }
                    .filter { it == true } // found
                    .map { doc }
        }

        fun clearCache()
        {
            documentsWithoutQueryText.clear();
        }

        private val documentsWithoutQueryText : HashSet<Uri> = HashSet();
    }
}