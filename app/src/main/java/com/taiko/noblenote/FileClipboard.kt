package com.taiko.noblenote

import rx.Observable
import rx.lang.kotlin.PublishSubject
import java.io.File

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

    private val cutFileList : MutableList<File> = java.util.ArrayList<File>();

    val hasContent: Boolean
        get() = !cutFileList.isEmpty()

    var contentOriginFolder: String = ""
        private  set

    val pastedFileNames: Observable<List<String>> = PublishSubject();

    fun pasteContentIntoFolder(targetFolderPath: File ) : Boolean
    {
        log.v("pasteContentIntoFolder targetFolderPath: $targetFolderPath");


        if(!targetFolderPath.isDirectory)
        {
            log.w("paste failed: targetFolderPath is not a directory: " + targetFolderPath.absoluteFile);
            return false;
        }

        var result = true;
        for (cutFile : File in cutFileList)
        {
            result = result && FileHelper.fileMoveToFolder(cutFile, targetFolderPath);
        }
        if(result)
        {
            (pastedFileNames as (rx.subjects.PublishSubject<List<String>>)).onNext(cutFileList.map { it.name });
            cutFileList.clear();
            contentOriginFolder = "";
        }
        return result;
    }

    fun cutFiles(files : List<File>)
    {
        cutFileList.clear();
        cutFileList.addAll(files);



        if(!files.isEmpty())
        {
            contentOriginFolder = files.first().parent;
            log.v("ContentOriginFolder: $contentOriginFolder");
        }
    }

    fun clearContent()
    {
        cutFileList.clear();
    }
}