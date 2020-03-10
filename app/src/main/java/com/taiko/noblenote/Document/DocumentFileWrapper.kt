package com.taiko.noblenote.Document

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.taiko.noblenote.loggerFor
import java.io.File


class DocumentFileWrapper : IDocumentFile {


    val log = loggerFor();

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

        val f = File(documentFile.uri.path);

        val target = File(f,displayName);
        target.mkdirs(); // createDirectory does not create subdirectories because it uses mkdir() instead of mkdirs() internally.
        val res = documentFile.createDirectory(displayName);
        if(res != null)
        {
            return DocumentFileWrapper(res);
        }
        return null;
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
        return documentFile.delete() // underlying RawDocumentFile implementation deletes the contents recursively
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

    @Deprecated("use File(uri.path)")
    private fun getFile() : File
    {
        val mFileField = documentFile.javaClass.getDeclaredField("mFile")
        mFileField.isAccessible = true;
        val res = mFileField.get(documentFile) as File
        return res;
    }


    override fun move(targetParentDocumentUri: Uri): Boolean
    {
        val targetDir = File(targetParentDocumentUri.path);

        if(!targetDir.isDirectory)
        {
            log.d("move failed: targetDir $targetDir is not a directory");
            return false;
        }

        val origFile = File(documentFile.uri.path);
        return origFile.renameTo(File(targetDir,origFile.name));
    }

    companion object {
        fun fromFile(file: File): IDocumentFile {
            return DocumentFileWrapper(DocumentFile.fromFile(file))
        }
    }

}