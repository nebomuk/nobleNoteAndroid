package com.taiko.noblenote.Document


import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.taiko.noblenote.*
import java.util.*
@TargetApi(24) // because of DocumentsContract.moveDocument
public class DocumentFileFast : IDocumentFile {


    val log = loggerFor()

    private var mDocumentid: String
    private val mParent: DocumentFileFast?
    private val mMimeType: String
    private var mDisplayName: String
    internal val mContext: Context


    private var mUri: Uri
    private var mFileListCached = false;
    private var mDisplayNameCached = false;

    private var mFileList: List<DocumentFileFast> = Collections.emptyList();

    constructor(parent: DocumentFileFast?, mContext: Context, mUri: Uri, displayName: String, mimeType: String) {
        this.mContext = mContext
        this.mUri = mUri
        this.mDisplayName = displayName;
        this.mMimeType = mimeType;
        this.mParent = parent;
        // TODO try to use DocumentsContract.getDocumentId(mUri);
        this.mDocumentid = DocumentsContract.getDocumentId(mUri);

    }

    constructor(parent: DocumentFileFast?, mContext: Context, mUri: Uri, displayName: String, mimeType: String, documentid : String)
            : this(parent,mContext,mUri,displayName,mimeType) {
        this.mDocumentid = documentid;
    }

    override val parentFile get() = mParent;

    override fun createFile(mimeType: String, displayName: String): IDocumentFile? {
        val result = createFile(mContext, mUri, mimeType, displayName)
        return if (result != null)
        {
            mFileListCached = false;
            DocumentFileFast(this, mContext, result, displayName, mimeType)
        }
        else null
    }

    override fun createDirectory(displayName: String): IDocumentFile? {
        val result = createFile(
                mContext, mUri, DocumentsContract.Document.MIME_TYPE_DIR, displayName)
        return if (result != null) {
            mFileListCached = false;
            DocumentFileFast(this, mContext, result, displayName, DocumentsContract.Document.MIME_TYPE_DIR)
        } else null
    }

    override val uri: Uri
        get() {
            return mUri
        }

    override val name: String?
        get() {
            if(mDisplayNameCached)
            {
                return mDisplayName;
            }
            else {
                mDisplayName = DocumentsContractApi19.getName(mContext, mUri).orEmpty();
                mDisplayNameCached = true;
                return mDisplayName;
            }
        }

    override val type: String?
        get() {
            return DocumentsContractApi19.getType(mContext, mUri)
        }

    override val isDirectory: Boolean
        get() {
            return DocumentsContractApi19.isDirectory(mContext, mUri)
        }

    override val isFile: Boolean
        get() {
            return DocumentsContractApi19.isFile(mContext, mUri)
        }

    override fun isVirtual(): Boolean {
        return DocumentsContractApi19.isVirtual(mContext, mUri)
    }

    override fun lastModified(): Long {
        return DocumentsContractApi19.lastModified(mContext, mUri)
    }

    override fun length(): Long {
        return DocumentsContractApi19.length(mContext, mUri)
    }

    override fun canRead(): Boolean {
        return DocumentsContractApi19.canRead(mContext, mUri)
    }

    override fun canWrite(): Boolean {
        return DocumentsContractApi19.canWrite(mContext, mUri)
    }

    override fun delete(): Boolean {
        return try {
            mDisplayNameCached = false;
            DocumentsContract.deleteDocument(mContext.contentResolver, mUri)
        } catch (e: Throwable) {
            false
        }
    }

    override fun exists(): Boolean {
        return DocumentsContractApi19.exists(mContext, mUri)
    }

    override fun listFiles(): List<IDocumentFile> {
        if (!mFileListCached) {
            mFileList = DocumentQuery.queryChildren(mContext, mUri, mDocumentid).map {
                val childUri = DocumentsContract.buildDocumentUriUsingTree(mUri, it.documentId);
                DocumentFileFast(this, mContext, mUri = childUri, displayName = it.name, mimeType = it.mimeType, documentid = it.documentId);
            }.toList();
            mFileListCached = true;
        }

        return mFileList;

    }

    override fun renameTo(displayName: String): Boolean {
        return try {
            val result = DocumentsContract.renameDocument(
                    mContext.contentResolver, mUri, displayName)
            if (result != null) {
                mUri = result
                mDocumentid = DocumentsContract.getDocumentId(mUri);
                mDisplayName = displayName;
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun findFile(displayName: String): IDocumentFile? {
        for (doc in listFiles()) {
            if (displayName == doc.name) {
                return doc
            }
        }
        return null
    }

    @TargetApi(24)
    override fun move(targetParentDocumentUri: Uri) : Boolean {
        if(mParent == null)
        {
            log.d("failed to move document: parent is null");
            return false;
        }

        val resUri =  DocumentsContract.moveDocument(mContext.contentResolver,mUri,mParent.mUri,targetParentDocumentUri);
        if(resUri == null)
        {
            log.d("failed to move document: DocumentsContract.moveDocument returned null");
            return false;
        }
        mUri = resUri;
        return true;
    }

    fun invalidateFileListCache()
    {
        mFileList = Collections.emptyList();
        mFileListCached = false;
    }


    companion object
        {

            fun createFile(context: Context, self: Uri, mimeType: String,
                           displayName: String): Uri? {
                return try {
                    DocumentsContract.createDocument(context.contentResolver, self, mimeType,
                            displayName)
                } catch (e: Exception) {
                    null
                }
            }

        private fun closeQuietly(closeable: AutoCloseable?) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (rethrown: RuntimeException) {
                    throw rethrown
                } catch (ignored: Exception) {
                }
            }
        }


        fun fromTreeUri(context: Context, treeUri: Uri): DocumentFileFast? {
            var documentId = DocumentsContract.getTreeDocumentId(treeUri)
            if (DocumentsContract.isDocumentUri(context, treeUri)) {
                documentId = DocumentsContract.getDocumentId(treeUri)
            }
            return DocumentFileFast(null, context,
                    DocumentsContract.buildDocumentUriUsingTree(treeUri,
                            documentId), "rootFolder", DocumentsContract.Document.MIME_TYPE_DIR);


        }
    }

}