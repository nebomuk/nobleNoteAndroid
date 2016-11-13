package com.taiko.noblenote

import rx.lang.kotlin.PublishSubject
import rx.subjects.PublishSubject
import java.io.File

/**
 * Created by fabdeuch on 04.10.2016.
 *
 * event bus to communicate between different ui components
 */
class UICommunicator
{
    public val  folderSelected : PublishSubject<File> = PublishSubject();
    public val  fileSelected : PublishSubject<File> = PublishSubject();
    val newFolderClick : PublishSubject<Unit> = PublishSubject();
    val newFileClick : PublishSubject<Unit> = PublishSubject();
    val fileCreated : PublishSubject<File> = PublishSubject()
    val folderCreated : PublishSubject<File> = PublishSubject()



}