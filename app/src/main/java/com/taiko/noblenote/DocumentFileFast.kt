package com.taiko.noblenote

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.File
import java.util.*


public class DocumentFileFast : IDocumentFile {


    private var mDocumentid: String
    private val mParent: DocumentFileFast?
    private val mMimeType: String
    private val mDisplayName: String
    private val mContext: Context
    private var mUri: Uri


    private var mFileListCached = false;
    private var mFileList: List<DocumentFileFast> = Collections.emptyList();

    constructor(parent: DocumentFileFast?, mContext: Context, mUri: Uri, displayName: String, mimeType: String) {
        this.mContext = mContext
        this.mUri = mUri
        this.log = loggerFor()
        this.mDisplayName = displayName;
        this.mMimeType = mimeType;
        this.mParent = parent;
        this.mDocumentid = DocumentsContract.getTreeDocumentId(mUri);

    }

    constructor(parent: DocumentFileFast?, mContext: Context, mUri: Uri, displayName: String, mimeType: String, documentid : String)
            : this(parent,mContext,mUri,displayName,mimeType) {
        this.mDocumentid = documentid;
    }

    override val parentFile get() = mParent;

    val log: InstanceLog

    override fun createFile(mimeType: String, displayName: String): IDocumentFile? {
        val result = createFile(mContext, mUri, mimeType, displayName)
        return if (result != null) DocumentFileFast(this, mContext, result, displayName, mimeType) else null
    }

    override fun createDirectory(displayName: String): IDocumentFile? {
        val result = createFile(
                mContext, mUri, DocumentsContract.Document.MIME_TYPE_DIR, displayName)
        return if (result != null) DocumentFileFast(this, mContext, result, displayName, DocumentsContract.Document.MIME_TYPE_DIR) else null
    }

    override val uri: Uri
        get() {
            return mUri
        }

    override val name: String?
        get() {
            return mDisplayName;
            //return DocumentsContractApi19.getName(mContext, mUri)
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
            DocumentsContract.deleteDocument(mContext.contentResolver, mUri)
        } catch (e: Exception) {
            false
        }
    }

    override fun exists(): Boolean {
        return DocumentsContractApi19.exists(mContext, mUri)
    }

    override fun listFiles(): List<IDocumentFile> {
        if (!mFileListCached) {
            mFileList = DocUtil.queryChildren(mUri,mDocumentid).map {
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

    fun createFile(context: Context, self: Uri, mimeType: String,
                            displayName: String): Uri? {
        return try {
            DocumentsContract.createDocument(context.contentResolver, self, mimeType,
                    displayName)
        } catch (e: Exception) {
            null
        }
    }
        companion object
        {

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
                            documentId),"rootFolder",DocumentsContract.Document.MIME_TYPE_DIR);


        }
    }

}