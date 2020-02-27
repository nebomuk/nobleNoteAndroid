package com.taiko.noblenote

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import com.taiko.noblenote.Document.DocumentFileFast
import com.taiko.noblenote.Document.DocumentFileWrapper
import com.taiko.noblenote.Document.IDocumentFile
import com.taiko.noblenote.Pref.rootPath
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.Collator
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class SFile {

    private val log = loggerFor();

    val nameWithoutExtension: String get()
    {
        return File(name).nameWithoutExtension
    }

    var doc : IDocumentFile

    private var parentDoc : IDocumentFile? = null;

    private var proposedFileName : String? = null

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
        if(foundFile != null)
        {
            parentDoc = parent.doc;
            doc = foundFile;
            cachedDoc.add(doc);
        }
        else
        {
            doc = parent.doc;
            proposedFileName = filename;
        }
    }

    constructor(document : IDocumentFile)
    {
        doc = document;
    }

    fun listFiles(): Array<out SFile> {

        val fileList =  doc.listFiles().map { SFile(it) }.toTypedArray()
        return fileList;


    }

    @Deprecated("replaced by DocumentFile.name")
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
            setDocumentToProposedIfExists();
            if(proposedFileName != null)
            {
                return proposedFileName.orEmpty();
            }
            return doc.name.orEmpty();
        }

    fun toFile(): File {
        var filePath: String? = null
        Log.d("", "URI = $uri")
        if ("content" == uri.scheme) {
            val cursor: Cursor = MainApplication.getInstance().contentResolver.query(uri, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)!!
            cursor.moveToFirst()
            filePath = cursor.getString(0)
            cursor.close()
        } else {
            filePath = uri.path
        }
        return File(filePath);
        Log.d("", "Chosen path = $filePath")
    }


    // use proper implementation to only show the names instead of creating sub-files
    // see https://stackoverflow.com/questions/41096332/issues-traversing-through-directory-hierarchy-with-android-storage-access-framew
    // and https://www.reddit.com/r/androiddev/comments/bbejc4/caveats_with_documentfile/
    fun listFilesSorted(folders: Boolean): List<SFile> {

        setDocumentToProposedIfExists()
        if(proposedFileName != null)
        {
            return Collections.emptyList();
        }

            //List<File> fileList = Arrays.asList(); // returns read only list, causes unsupported operation exceptions in adapter
            val fileList = ArrayList<SFile>()
            Collections.addAll(fileList, *this.listFiles())
            Collections.sort(fileList) { lhs, rhs -> Collator.getInstance().compare(lhs.name, rhs.name) }

            return fileList
        }

    fun lastModified(): Long {
        setDocumentToProposedIfExists()
       if(proposedFileName != null)
       {
           return 0L;
       }

        return this.doc.lastModified();
    }

    fun exists() : Boolean {
        setDocumentToProposedIfExists()
        if(proposedFileName != null)
        {
            return false;
        }

        return doc.exists();
    }

    private fun setDocumentToProposedIfExists()
    {
        if(proposedFileName != null)
        {
            var res = doc.findFile(proposedFileName!!);
            if(res != null)
            {
                parentDoc = doc;
                doc = res;
                proposedFileName = null;
                cachedDoc.add(res);
            }
        }
    }

    fun mkdir() : Boolean {
        if(proposedFileName == null)
        {
            log.v("mkdir() failed, proposed file is null")
            return false; // the doc points to a file that exists
        }
        else
        {
            // doc points to a parent, try to create a directory
            var res = doc.createDirectory(proposedFileName!!);
            if(res == null)
            {
                log.v("mkdir failed, DocumentFile.createDirectory returned null")
                return false;
            }
            else
            {
                parentDoc = doc;
                doc = res;
                proposedFileName = null;
                cachedDoc.add(doc);
                return true;
            }
        }
    }

    val isDirectory : Boolean  get()
    {
        setDocumentToProposedIfExists();
        if(proposedFileName != null)
        {
            return false;
        }
        return doc.isDirectory;
    }

    val isFile : Boolean  get()
    {
        setDocumentToProposedIfExists();
        if(proposedFileName != null)
        {
            return false;
        }
        return !doc.isDirectory;
    }

    /***
     * renames files and folders, works on folders with contents.
     */
    fun renameTo(newName : String): Boolean {
        setDocumentToProposedIfExists();
        if(proposedFileName != null)
        {
            log.v("renameTo failed, file does not exist");
            return false;
        }
        return doc.renameTo(newName);

    }

    fun move(targetParent : Uri) : Boolean
    {
        setDocumentToProposedIfExists();
        if(proposedFileName != null)
        {
            log.v("move failed, file does not exist");
            return false;
        }
        val moved =  doc.move(targetParent);
        if(moved && doc.parentFile is DocumentFileFast)
        {
            invalidateAllFileListCaches();
        }
        return moved;
    }

    fun openInputStream() : InputStream {
        // TODO create proposed file if it does not exist
        return doc.openInputStream();
    }

    fun openOuptutStream() : OutputStream {
        // TODO create proposed file if it does not exist
        return doc.openOutputStream();
    }

    fun createNewFile(): Boolean {
        setDocumentToProposedIfExists();
        if(proposedFileName != null)
        {
            var res = doc.createFile("text", proposedFileName!!);
            if(res != null)
            {
                parentDoc = doc;
                doc = res;
                return true;
            }
            else
            {
                log.v("could not create file, DocumentFile.createFile returned null")
                return false;
            }
        }
        log.v("Could not create file, file already exists");
        return false;
    }

    fun deleteRecursively(): Boolean {
        TODO("delete Recursively not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val parentFile : SFile get() {
        if(parentDoc == null)
        {
            if(this.doc.parentFile != null)
            {
                return SFile(doc.parentFile!!);
            }
            else if(doc.uri == rootUri)
            {
                throw UnsupportedOperationException("parentFile cannot be accessed because this file is already the root of the document tree");
            }
            else
            {
                // some hacky attempts to reconstruct parent files exist, see
                // https://stackoverflow.com/questions/33909305/construct-uri-for-android-5-0-documentfile-from-action-open-document-tree-root-u
                // or getParentDocument https://github.com/rcketscientist/DocumentActivity/blob/master/library/src/main/java/com/anthonymandra/framework/UsefulDocumentFile.java
                throw UnsupportedOperationException("parentFile could not be found");
            }
        }
        else
        {
            return SFile(parentDoc!!);
        }
    }

    companion object
    {

    private fun toDocumentFile(uri : Uri) : IDocumentFile
    {
        if(SFile.cachedDoc.any { it.uri == uri })
                {
                    return cachedDoc.first { it.uri == uri }
                }

        var doc : IDocumentFile

        if(uri.scheme == "file")
        {
            doc =  DocumentFileWrapper.fromFile(File(uri.path));
        }
        else if(uri.scheme == "content")
        {
            doc = DocumentFileFast.fromTreeUri(MainApplication.getInstance(),uri)!!;
        }
        else
        {
            throw UnsupportedOperationException(("Uri scheme not supported:" + uri.scheme));
        }

        cachedDoc.add(doc);
        return doc;


    }
        private val cachedDoc : HashSet<IDocumentFile> = HashSet();

        // dangerous! should only be called when rootPath changed, because most of the logic depends on the cache
        fun clearCache()
        {
            cachedDoc.clear();
        }

        fun invalidateAllFileListCaches() {
            cachedDoc.forEach {
                val fast = it as? DocumentFileFast;
                fast?.invalidateFileListCache();
            }
        }
    }

}


fun IDocumentFile.openInputStream(): InputStream {

    if(this is DocumentFileFast)
    {
        return this.mContext.contentResolver.openInputStream(this.uri)!!;
    }
    val file = File(this.uri.path);
    return file.inputStream();

}

fun IDocumentFile.openOutputStream(): OutputStream {
    if(this is DocumentFileFast)
    {
        return this.mContext.contentResolver.openOutputStream(this.uri)!!;
    }
    val file = File(this.uri.path);
    return file.outputStream();
}

public fun File.toSFile(): SFile {
    val uri = DocumentFileWrapper.fromFile(this).uri;
    return SFile(uri);
}