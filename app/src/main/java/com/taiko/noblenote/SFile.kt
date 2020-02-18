package com.taiko.noblenote

import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.commonsware.cwac.document.DocumentFileCompat
import com.taiko.noblenote.Pref.rootPath
import java.io.File
import java.text.Collator
import java.util.*
import kotlin.collections.HashSet


class SFile {

    val doc : DocumentFile

    val uri : Uri get() {
        val u =  doc.uri
        return u;
    }

    val rootUri: Uri
        get() = Uri.parse(rootPath.value)

    constructor(uriString: String?) {
        val parsedUri = Uri.parse(uriString);
        doc = toDocumentFile(parsedUri);

    }

    constructor(uri : Uri)
    {
        doc = toDocumentFile(uri);
    }

    constructor(parent: SFile, filename: String) {

        val foundFile = parent.doc.findFile(filename);
        doc = foundFile!!;
    }

    constructor(document : DocumentFile)
    {
        doc = document;
    }

    fun listFiles(): Array<out SFile> {

        val fileList =  doc.listFiles().map { SFile(it) }.toTypedArray()
        return fileList;


    }

    private fun displayName(uri: Uri): String {
        val mCursor: Cursor = MainApplication.getInstance().contentResolver.query(uri, null, null, null, null)!!
        val indexedname = mCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        mCursor.moveToFirst()
        val filename = mCursor.getString(indexedname)
        mCursor.close()
        return filename
    }

    val name: String
        get() {
            return displayName(uri);
        }

    val path: String
        get() = uri.toString()

    fun toFile(): File {
        var filePath: String? = null
        Log.d("", "URI = $uri")
        if ("content" == uri.scheme) {
            val cursor: Cursor = MainApplication.getInstance().getContentResolver().query(uri, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)!!
            cursor.moveToFirst()
            filePath = cursor.getString(0)
            cursor.close()
        } else {
            filePath = uri.path
        }
        return File(filePath);
        Log.d("", "Chosen path = $filePath")
    }

    fun listFilesSorted(folders: Boolean): List<SFile> {
            //List<File> fileList = Arrays.asList(); // returns read only list, causes unsupported operation exceptions in adapter
            val fileList = ArrayList<SFile>()
            Collections.addAll(fileList, *this.listFiles())
            Collections.sort(fileList) { lhs, rhs -> Collator.getInstance().compare(lhs.name, rhs.name) }

            return fileList
        }

    companion object
    {

    private fun toDocumentFile(uri : Uri) : DocumentFile
    {
        if(SFile.cachedDoc.any { it.uri == uri })
                {
                    return cachedDoc.first { it.uri == uri }
                }

        var doc : DocumentFile

        if(uri.scheme == "file")
        {
            doc =  DocumentFile.fromFile(File(uri.path));
        }
        else if(uri.scheme == "content")
        {
            doc = DocumentFile.fromTreeUri(MainApplication.getInstance(),uri)!!;
        }
        else
        {
            throw UnsupportedOperationException(("Uri scheme not supported:" + uri.scheme));
        }

        cachedDoc.add(doc);
        return doc;


    }
        private val cachedDoc : HashSet<DocumentFile> = HashSet();
    }

}

public fun File.toSFile(): SFile {
    val uri = DocumentFile.fromFile(this).uri;
    return SFile(uri);
}