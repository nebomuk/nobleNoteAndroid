package com.taiko.noblenote

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import android.text.InputFilter
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.io.IOException

/**
 * @author Taiko
 *
 * shows dialogs to create new notes or folders
 * the path are determined using the app's shared prefs where the current folder's path and the root path are stored
 */

object Dialogs {

    @JvmStatic
    fun showNewNoteDialog(layout: CoordinatorLayout, fileCreated: (f : SFile) -> Unit) {

        val context = layout.context;

            val
                 dialogBuilder = AlertDialog.Builder(context)

                    val rootContents = SFile(Pref.rootPath.value).listFiles();
                    if(rootContents.isEmpty())
                    {
                        dialogBuilder.setTitle(R.string.title_noNotebookExists)
                        dialogBuilder.setMessage(R.string.noNotebookExists)
                        dialogBuilder.setPositiveButton(
                                android.R.string.ok,null)
                        dialogBuilder.show();
                        return;
                    }

                    dialogBuilder.setTitle(R.string.newNote)
                    dialogBuilder.setMessage(R.string.enterName)

                    // Set an EditText view to get user input
                    val input = EditText(context)
                    input.filters = arrayOf<InputFilter>(FileNameFilter())

                    val parent = SFile(Pref.rootPath.value);
                    val proposedFileName = context.getString(R.string.newNote)
                    var proposed = SFile(parent, proposedFileName)
                    var counter = 0
                    while (proposed.exists()) {
                    proposed = SFile(parent,"$proposedFileName (${++counter})")
                    }

                    input.setText(proposed.name)
                    input.setSelection(input.text.length)
                    dialogBuilder.setView(wrapWithMargins(input))
                    dialogBuilder.setPositiveButton(
                            android.R.string.ok) { dialog, whichButton ->
                        val newName = input.text.trim()
                        val newFile = SFile(SFile(Pref.currentFolderPath.value), newName.toString())
                        try {
                            if(newFile.exists())
                            {
                                Snackbar.make(layout, R.string.notCreatedNoteExists, Snackbar.LENGTH_LONG).show()
                            }
                            else if (newFile.createNewFile()) {
                                fileCreated(newFile)
                            } else
                            // error occured
                            {
                                Snackbar.make(layout, R.string.noteNotCreated, Snackbar.LENGTH_SHORT).show()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    // does nothing but creates a button
                    dialogBuilder.setNegativeButton(android.R.string.cancel, null)

                    dialogBuilder.show()

    }

    /**
     * wraps the given EditText in a FrameLayout and adds margins on the sides
     */
    private fun wrapWithMargins(input : EditText) : FrameLayout
    {
        val container = FrameLayout(input.context)
        val paramss = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        val margin = ScreenUtil.dpToPx(input.context,16);
        paramss.marginStart = margin;
        paramss.marginEnd = margin;
        input.layoutParams = paramss;
        container.addView(input);
        return container;
    }

    @JvmStatic
    fun showNewFolderDialog(layout: CoordinatorLayout, folderCreated: (f : SFile) -> Unit) {

        val context = layout.context;
                    val dialogBuilder = AlertDialog.Builder(context)

                    dialogBuilder.setTitle(R.string.newNotebook)
                    dialogBuilder.setMessage(R.string.enterName)

                    // Set an EditText view to get user input
                    val input = EditText(context)
                    input.filters = arrayOf<InputFilter>(FileNameFilter())

                    val parent = SFile(Pref.rootPath.value);
                    val proposedFileName = context.getString(R.string.newNotebook)
                    var proposed = SFile(parent, proposedFileName)
                    var counter = 0
                    while (proposed.exists()) {
                        proposed = SFile(parent,"$proposedFileName (${++counter})")
                    }
                    input.setText(proposed.name)
                    input.setSelection(input.text.length)
                    dialogBuilder.setView(wrapWithMargins(input))

                    dialogBuilder.setPositiveButton(
                            android.R.string.ok) { dialog, whichButton ->
                        val newName = input.text.trim()
                        val dir = SFile(SFile(Pref.rootPath.value), newName.toString())
                        if(dir.exists())
                        {
                            Snackbar.make(layout, R.string.notCreatedNotebookExists, Snackbar.LENGTH_LONG).show()
                        }
                        else if (!dir.mkdir()) {
                            Snackbar.make(layout, R.string.notebookNotCreated, Snackbar.LENGTH_LONG).show()
                        } else {
                            folderCreated(dir)
                        }
                    }

                    // does nothing but creates a button
                    dialogBuilder.setNegativeButton(android.R.string.cancel, null)

                    dialogBuilder.show();

    }

    /**
     * shows a dialog to rename a folder or file relative to the root path
     */
    @JvmStatic
    fun showRenameDialog(activity : Activity, rootView: View, file: SFile, onRenamed : (renamedFile: SFile) -> Unit, onNotRenamed : () -> Unit)
    {

                    val dialogBuilder = AlertDialog.Builder(rootView.context)

                    val isDir: Boolean = file.isDirectory

                    val title = if (isDir) R.string.renameNotebook else R.string.renameNote
                    val msgExists = if (isDir) R.string.notebookExists else R.string.noteExists
                    val message = R.string.enterNewName
                    val msgNotRenamed = if (isDir) R.string.notebookNotRenamed else R.string.noteNotRenamed


                    dialogBuilder.setTitle(title)
                    dialogBuilder.setMessage(message)

                    // Set an EditText view to get user input
                    val input = EditText(rootView.context)
                    input.filters = arrayOf(FileNameFilter())
                    input.setText(file.name)
                    input.setSelection(input.text.length)
                    dialogBuilder.setView(wrapWithMargins(input))

                    dialogBuilder.setPositiveButton(
                            android.R.string.ok) { dialog, whichButton ->
                        val newName = input.text.toString().trim()


                        if (newName == file.name) // name wasnt changed by user
                        {
                            onNotRenamed();
                            return@setPositiveButton;
                        }

                        var proposed = SFile(file.parentFile,newName)
                        if (proposed.exists()) {
                            Snackbar.make(rootView, msgExists, Snackbar.LENGTH_LONG).show()
                            onNotRenamed();
                            return@setPositiveButton
                        }

                        Observable.create<Boolean>
                        {
                            var renamed = false;

                            renamed = file.renameTo(newName)

                            it.onNext(renamed);
                            it.onCompleted();

                        }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    val renamed = it;
                                    if (!renamed) {
                                        Snackbar.make(rootView, msgNotRenamed, Snackbar.LENGTH_LONG).show()
                                        onNotRenamed();

                                    } else {
                                        onRenamed(file) // TODO check if the underlying DocumentFile has been modified in place
                                    }
                                }
                    }


                    dialogBuilder.setNegativeButton(android.R.string.cancel, { dialogInterface: DialogInterface, i: Int -> onNotRenamed() });
                    dialogBuilder.setOnCancelListener { onNotRenamed() }

                    dialogBuilder.show()
    }



}
