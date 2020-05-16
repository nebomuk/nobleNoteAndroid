package com.taiko.noblenote

import com.taiko.noblenote.filesystem.SFile
import com.taiko.noblenote.util.loggerFor
import rx.Observable
import rx.lang.kotlin.PublishSubject

/**
 * Created by taiko
 *
 * shared clipboard state between classes that require the list of "cut" files
 * that are currently in the clipboard
 *
 * file cut and paste similar to Amaze File Explorer
 */
object FileClipboard {

    private val log = loggerFor();

    private val cutFileList : MutableList<SFile> = java.util.ArrayList();

    val hasContent: Boolean
        get() = cutFileList.isNotEmpty()

    var contentOriginFolder: SFile? = null
        private  set

    val pastedFileNames: Observable<List<String>> = PublishSubject();

    fun pasteContentIntoFolder(targetFolderPath: SFile) : Boolean
    {
        log.v("pasteContentIntoFolder targetFolderPath: $targetFolderPath");


        if(!targetFolderPath.isDirectory)
        {
            log.w("paste failed: targetFolderPath is not a directory: ${targetFolderPath.uri}");
            return false;
        }

        var result = true;
        for (cutFile : SFile in cutFileList)
        {

            result = result &&  cutFile.move(targetFolderPath.uri);
        }
        if(result)
        {
            (pastedFileNames as (rx.subjects.PublishSubject<List<String>>)).onNext(cutFileList.map { it.name });
            cutFileList.clear();
            contentOriginFolder = null;
        }
        return result;
    }

    fun cutFiles(files : List<SFile>)
    {
        cutFileList.clear();
        cutFileList.addAll(files);



        if(!files.isEmpty())
        {
            contentOriginFolder = files.first().parentFile;
            log.v("ContentOriginFolder: $contentOriginFolder");
        }
    }

    fun clearContent()
    {
        cutFileList.clear();
    }
}