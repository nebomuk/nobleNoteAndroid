package com.taiko.noblenote

import android.app.AlertDialog
import android.content.Context
import android.text.InputFilter
import android.widget.EditText
import android.widget.Toast
import java.io.File
import java.io.IOException

/**
 * @author Taiko
 */

object Dialogs {

    @JvmStatic
    fun showNewNoteDialog(activity: Context, fileCreated : (f : File) -> Unit) {
        val dialogBuilder = AlertDialog.Builder(activity)

        dialogBuilder.setTitle(R.string.newNote)
        dialogBuilder.setMessage(R.string.enterName)

        // Set an EditText view to get user input
        val input = EditText(activity)
        input.filters = arrayOf<InputFilter>(FileNameFilter())

        // dont propose a name that already exists
        var proposed = File(Pref.currentFolderPath.value, activity.getString(R.string.newNote))
        var counter = 0
        while (proposed.exists()) {
            proposed = File("${proposed.absoluteFile} (${++counter})")
        }
        input.setText(proposed.name)
        input.setSelection(input.text.length)
        dialogBuilder.setView(input)
        dialogBuilder.setPositiveButton(
                android.R.string.ok) { dialog, whichButton ->
            val newName = input.text.trim()
            val newFile = File(Pref.currentFolderPath.value, newName.toString())
            try {
                if (newFile.createNewFile()) {
                    fileCreated(newFile)
                } else
                // error occured
                {
                    Toast.makeText(activity, R.string.noteNotCreated, Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        // does nothing but creates a button
        dialogBuilder.setNegativeButton(android.R.string.cancel, null)

        dialogBuilder.show()
    }

    @JvmStatic
    fun showNewFolderDialog(activity: Context, folderCreated : (f : File) -> Unit) {
        val dialogBuilder = AlertDialog.Builder(activity)

        dialogBuilder.setTitle(R.string.newNotebook)
        dialogBuilder.setMessage(R.string.enterName)

        // Set an EditText view to get user input
        val input = EditText(activity)
        input.filters = arrayOf<InputFilter>(FileNameFilter())

        val proposedDirPath = File(Pref.rootPath.value, activity.getString(R.string.newNotebook)).absolutePath
        var proposed = File(proposedDirPath)
        var counter = 0
        while (proposed.exists()) {
            proposed = File("$proposedDirPath  (${++counter})")
        }
        input.setText(proposed.name)
        input.setSelection(input.text.length)
        dialogBuilder.setView(input)

        dialogBuilder.setPositiveButton(
                android.R.string.ok) { dialog, whichButton ->
            val newName = input.text.trim()
            val dir = File(Pref.rootPath.value, newName.toString())
            if (!dir.mkdirs()) {
                Toast.makeText(activity, R.string.notebookNotCreated, Toast.LENGTH_SHORT).show()
            }
            else
            {
                folderCreated(dir)
            }
        }

        // does nothing but creates a button
        dialogBuilder.setNegativeButton(android.R.string.cancel, null)

        dialogBuilder.show()
    }
}
