package com.taiko.noblenote

import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract

object DocUtil {

    data class CursorResult(val name : String, val mimeType : String, val documentId : String)


    fun queryChildren(treeUri : Uri, documentId : String): List<CursorResult> {
        val documentUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri,
                documentId);

        createCursor(documentUri).use {
            val res = getResultsFromCursor(it!!);
            return res;
        }


    }

    fun queryFromRawUri(rawUri : Uri): List<CursorResult> {
        // TODO check if these lines can be uncommented
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(rawUri,
                DocumentsContract.getTreeDocumentId(rawUri))

        createCursor(childrenUri).use {
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

            res.add(CursorResult(str,mime,docId));
        }

        return res;
    }


    private fun createCursor(uri : Uri): Cursor? {
        val cursor = MainApplication.getInstance().contentResolver.query(
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