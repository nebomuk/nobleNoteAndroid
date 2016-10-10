package com.taiko.noblenote

import rx.lang.kotlin.PublishSubject
import rx.subjects.PublishSubject
import java.io.File

/**
 * Created by fabdeuch on 04.10.2016.
 */
class UICommunicator
{
    public val  folderSelected : PublishSubject<File> = PublishSubject();
    public val  fileSelected : PublishSubject<File> = PublishSubject();
}