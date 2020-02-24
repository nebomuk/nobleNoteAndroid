package com.taiko.noblenote

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

class DocumentFileWrapper : IDocumentFile {


    private val documentFile: DocumentFile

    internal constructor(documentFile: DocumentFile) {
        this.documentFile = documentFile
    }

    override val parentFile: IDocumentFile?
        get() = if(documentFile.parentFile == null) null else DocumentFileWrapper(documentFile.parentFile!!)

    override val uri: Uri
        get() = documentFile.uri

    override val name: String?
        get() = documentFile.name

    override val type: String?
        get() = documentFile.type

    override val isDirectory: Boolean
        get() = documentFile.isDirectory

    override val isFile: Boolean
        get() = documentFile.isFile

    override fun createFile(mimeType: String, displayName: String): IDocumentFile? {
        val  res = documentFile.createFile(mimeType, displayName);
        if(res != null)
        {
            return DocumentFileWrapper(res)
        }
        else
        {
            return null;
        }
    }

    override fun createDirectory(displayName: String): IDocumentFile? {
        val res = documentFile.createDirectory(displayName);
        if(res == null)
        {
            return null;
        }
        return DocumentFileWrapper(res);
    }

    override fun isVirtual(): Boolean {
        return documentFile.isVirtual
    }

    override fun lastModified(): Long {
        return documentFile.lastModified()
    }

    override fun length(): Long {
        return documentFile.length()
    }

    override fun canRead(): Boolean {
        return documentFile.canRead()
    }

    override fun canWrite(): Boolean {
        return documentFile.canWrite()
    }

    override fun delete(): Boolean {
        return documentFile.delete()
    }

    override fun exists(): Boolean {
        return documentFile.exists()
    }

    override fun listFiles(): List<IDocumentFile> {
        return documentFile.listFiles().map { DocumentFileWrapper(it) }
    }

    override fun renameTo(displayName: String): Boolean {
        return false
    }

    override fun findFile(displayName: String): IDocumentFile? {
        val res = documentFile.findFile(displayName);
        if(res == null)
        {
            return null;
        }
        return DocumentFileWrapper(res);
    }

    companion object {
        fun fromFile(file: File): IDocumentFile {
            return DocumentFileWrapper(DocumentFile.fromFile(file))
        }
    }

}