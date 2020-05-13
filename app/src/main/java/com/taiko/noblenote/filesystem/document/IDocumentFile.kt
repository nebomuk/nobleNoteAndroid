package com.taiko.noblenote.filesystem.document

import android.net.Uri

interface IDocumentFile {

    val parentFile: IDocumentFile?
    val uri: Uri
    val name: String?
    val type: String?
    val isDirectory: Boolean
    val isFile: Boolean

    fun createFile(mimeType: String, displayName: String): IDocumentFile?
    fun createDirectory(displayName: String): IDocumentFile?
    fun isVirtual(): Boolean
    fun lastModified(): Long
    fun length(): Long
    fun canRead(): Boolean
    fun canWrite(): Boolean
    fun delete(): Boolean
    fun exists(): Boolean
    fun listFiles(): List<IDocumentFile>
    fun renameTo(displayName: String): Boolean
    fun findFile(displayName: String): IDocumentFile?
    fun move(targetParentDocumentUri : Uri) : Boolean

}