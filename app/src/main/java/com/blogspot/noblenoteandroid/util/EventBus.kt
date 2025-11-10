package com.blogspot.noblenoteandroid.util

import com.blogspot.noblenoteandroid.filesystem.SFile
import rx.lang.kotlin.PublishSubject
import rx.subjects.PublishSubject

/**
 * Created by Taiko on 04.10.2016.
 *
 * event bus to communicate between different ui components
 */
class EventBus
{
    val  fileSelected : PublishSubject<SFile> = PublishSubject();
    val createFileClick: PublishSubject<SFile> = PublishSubject()
    val createFolderClick: PublishSubject<SFile> = PublishSubject()
    val fabMenuVisible : PublishSubject<Int /*View.Visible*/> = PublishSubject();
    val swipeRefresh: PublishSubject<Unit> = PublishSubject()



}