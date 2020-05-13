package com.taiko.noblenote.filesystem.document

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract

object DocumentQuery {

    data class CursorResult(val name : String, val mimeType : String, val documentId : String)


    fun queryChildren(context : Context, treeUri : Uri, documentId : String): List<CursorResult> {
        val documentUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri,
                documentId);

        createCursor(documentUri, context).use {
            val res = getResultsFromCursor(it!!);
            return res;
        }


    }

    @Deprecated("Use queryChildren with DocumentsContract.getTreeDcoumentId(uri) as documentId")
    fun queryFromRawUri(rawUri: Uri, context: Context): List<CursorResult> {
        // TODO check if these lines can be uncommented
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(rawUri,
                DocumentsContract.getTreeDocumentId(rawUri))

        createCursor(childrenUri, context = context).use {
            val res = getResultsFromCursor(it!!);
            return res;
        }
    }


    private fun getResultsFromCursor(cursor : Cursor) : List<CursorResult>
    {


        val res = ArrayList<CursorResult>();

        while (cursor.moveToNext()) {
            val str = cursor.getString(0);
            val mime = cursor.getString(1);
            val docId = cursor.getString(2);

            res.add(CursorResult(str, mime, docId));
        }

        return res;
    }


    private fun createCursor(uri: Uri, context: Context): Cursor? {
        val cursor = context.contentResolver.query(
                uri, arrayOf(
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_DOCUMENT_ID),
                null,
                null,
                null)

        return cursor;
    }
}