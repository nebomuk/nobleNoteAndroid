package com.taiko.noblenote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

/**
 * Created by fabdeuch on 23.09.2016.
 */

public class Dialogs
{
    public void showNewNoteDialog(final Context activity, final FileSystemAdapter fileSystemAdapter)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

        dialogBuilder.setTitle(R.string.newNote);
        dialogBuilder.setMessage(R.string.enterName);

        // Set an EditText view to get user input
        final EditText input = new EditText(activity);
        input.setFilters(new InputFilter[]{new FileNameFilter()});

        // dont propose a name that already exists
        String proposedFilePath = fileSystemAdapter.getRootDir().getPath() + File.separator + activity.getString(R.string.newNote);
        File proposed = new File(proposedFilePath);
        int counter = 0;
        while (proposed.exists())
        {
            proposed = new File(proposedFilePath + " (" + ++counter + ")");
        }
        input.setText(proposed.getName());
        input.setSelection(input.getText().length());
        dialogBuilder.setView(input);
        dialogBuilder.setPositiveButton(
                android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        Editable newName = input.getText();
                        File newFile = new File(fileSystemAdapter.getRootDir().getPath() + File.separator + newName);
                        if (fileSystemAdapter.createFile(newFile) == 0)
                        {
                            NoteListFragment.startNoteEditor(activity, newFile);
                        }
                        else // error occured
                        {
                            Toast.makeText(activity, R.string.noteNotCreated, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // does nothing but creates a button
        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
            }
        });

        dialogBuilder.show();
    }

    public void showNewFolderDialog(final Context activity, final FileSystemAdapter fileSystemAdapter)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

        dialogBuilder.setTitle(R.string.newNotebook);
        dialogBuilder.setMessage(R.string.enterName);

        // Set an EditText view to get user input
        final EditText input = new EditText(activity);
        input.setFilters(new InputFilter[]{new FileNameFilter()});

        String proposedDirPath = Pref.INSTANCE.getRootPath() + File.separator + activity.getString(R.string.newNotebook);
        File proposed = new File(proposedDirPath);
        int counter = 0;
        while(proposed.exists())
        {
            proposed = new File(proposedDirPath + " (" + ++counter +")");
        }
        input.setText(proposed.getName());
        input.setSelection(input.getText().length());
        dialogBuilder.setView(input);

        dialogBuilder.setPositiveButton(
                android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        Editable newName = input.getText();
                        if(fileSystemAdapter.mkdir(new File(Pref.INSTANCE.getRootPath() + File.separator + newName)) != 0)
                        {
                            Toast.makeText(activity, R.string.notebookNotCreated, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // does nothing but creates a button
        dialogBuilder.setNegativeButton(android.R.string.cancel,null);

        dialogBuilder.show();
    }
}
