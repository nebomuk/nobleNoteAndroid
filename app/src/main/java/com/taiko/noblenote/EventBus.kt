package com.taiko.noblenote

import rx.lang.kotlin.PublishSubject
import rx.subjects.PublishSubject
import java.io.File

/**
 * Created by Taiko on 04.10.2016.
 *
 * event bus to communicate between different ui components
 */
class EventBus
{
    val  fileSelected : PublishSubject<File> = PublishSubject();
    val createFileClick: PublishSubject<File> = PublishSubject()
    val createFolderClick: PublishSubject<File> = PublishSubject()
    val filesPasted : PublishSubject<Sequence<File>> = PublishSubject();
    val swipeRefresh: PublishSubject<Unit> = PublishSubject()



}