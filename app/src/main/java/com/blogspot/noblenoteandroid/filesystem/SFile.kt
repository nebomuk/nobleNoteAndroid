package com.blogspot.noblenoteandroid.filesystem

import android.content.Context
import android.net.Uri
import com.blogspot.noblenoteandroid.filesystem.document.DocumentFileFast
import com.blogspot.noblenoteandroid.filesystem.document.DocumentFileWrapper
import com.blogspot.noblenoteandroid.filesystem.document.IDocumentFile
import com.blogspot.noblenoteandroid.util.loggerFor
import rx.subjects.BehaviorSubject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.Collator
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/***
 * java.io.File like API wrapper around storage access framework's content uris and DocumentFileFast
 *
 * you need to call SFile.register(context) before using this class
 */

class SFile {

    private val log = loggerFor();

    override fun equals(other: Any?): Boolean {
        return (other as? SFile)?.uri == this.uri;
    }

    override fun hashCode(): Int {
        return uri.hashCode();
    }

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

    constructor(uriString: String) {
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


    val name: String
        get() {

            // this optimization is needed because RawDocumentFile calls listFiles()
            // every time getName() or setDocumentToProposedIfExists() is used and this is extremely slow
            if(uri.scheme == "file")
            {
                if(proposedFileName != null)
                {
                    return proposedFileName.orEmpty();
                }

                return File(uri.path).name;
            }

            setDocumentToProposedIfExists();
            if(proposedFileName != null)
            {
                return proposedFileName.orEmpty();
            }
            return doc.name.orEmpty();
        }


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
                proposedFileName = null;
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
        return doc.delete()
    }

    val parentFile : SFile
        get() {
        if(parentDoc == null)
        {
            if(this.doc.parentFile != null)
            {
                return SFile(doc.parentFile!!);
            }
            else if(doc.uri == Uri.parse(rootPathSubject.value))
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
        if(cachedDoc.any { it.uri == uri })
                {
                    return cachedDoc.first { it.uri == uri }
                }

        var doc : IDocumentFile

        if(uri.scheme == "file")
        {
            doc = DocumentFileWrapper.fromFile(File(uri.path));
        }
        else if(uri.scheme == "content")
        {
            doc = DocumentFileFast.fromTreeUri(context, uri)!!;
        }
        else
        {
            throw UnsupportedOperationException(("Uri scheme not supported, Uri:" + uri.toString()));
        }

        cachedDoc.add(doc);
        return doc;


    }
        private val cachedDoc : HashSet<IDocumentFile> = HashSet();


        @JvmStatic
        fun invalidateAllFileListCaches() {
            cachedDoc.forEach {
                val fast = it as? DocumentFileFast;
                fast?.invalidateFileListCache();
            }
        }

        /*
         * dangerous! should only be called when rootPath changed, because most of the logic depends on the cache
         */
        @JvmStatic
        fun clearGlobalDocumentCache()
        {
            cachedDoc.clear();
        }

        /*
        * this methods needs to be called once before using SFile
         */
        @JvmStatic
        fun register(context : Context, rootPath : BehaviorSubject<String>)
        {
            Companion.context = context.applicationContext;
            rootPathSubject = rootPath;
        }

        private lateinit var rootPathSubject: BehaviorSubject<String>

        private lateinit var context : Context;
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
        return this.mContext.contentResolver.openOutputStream(this.uri,"rwt")!!; // w : write, wa: write append, rwt replace write truncate? is required so that the existing content is completely replaced
    }
    val file = File(this.uri.path);
    return file.outputStream();
}

public fun File.toSFile(): SFile {
    val uri = DocumentFileWrapper.fromFile(this).uri;
    return SFile(uri);
}