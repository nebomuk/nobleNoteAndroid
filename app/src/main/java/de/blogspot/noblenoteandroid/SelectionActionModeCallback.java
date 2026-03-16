package de.blogspot.noblenoteandroid;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.text.style.StrikethroughSpan;
import android.text.style.UnderlineSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import de.blogspot.noblenoteandroid.editor.DroidWriterEditText;
import de.blogspot.noblenoteandroid.editor.Format;


/**
 * 
 * @author Taiko-G780
 *
 * a selection contextual action mode with cut, copy and paste and additional formatting buttons
 * for Honeycomb and newer devices *
 */

// This class cannot use Android X because as of April 2020, TextViewCompat does not supportin the Android X action mode callback
public class SelectionActionModeCallback implements android.view.ActionMode.Callback {

	
	private DroidWriterEditText editText;
    private ShareActionProvider mShareActionProvider;

    public SelectionActionModeCallback(DroidWriterEditText editText) {
		super();
		this.editText = editText;
	}

    @Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.selection_cab, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            mShareActionProvider = new ShareActionProvider(editText.getContext());
            item.setActionProvider(mShareActionProvider);
        }
        else
        {
            item.setVisible(false); // share is available on newer Android versions anyways
        }

        return true;
    }
	@Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

        editText.setWindowFocusWait(true);
        return false;
    }
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.bold) {
            Format.toggleStyleSpan(this.editText, Typeface.BOLD);
            editText.setModified(true); // apparently doesnt get set automatically by DroidWriterEditText's TextWatcher
            return true;
        } else if (itemId == R.id.italic) {
            Format.toggleStyleSpan(this.editText, Typeface.ITALIC);
            editText.setModified(true);
            return true;
        } else if (itemId == R.id.underline) {
            Format.toggleCharacterStyle(this.editText, UnderlineSpan.class);
            editText.setModified(true);
            return true;
        } else if (itemId == R.id.strikethrough) {
            Format.toggleCharacterStyle(this.editText, StrikethroughSpan.class);
            editText.setModified(true);
            return true;
        } else if (itemId == R.id.menu_item_share) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)   // item is not visible
            {
                return false;
            }
            int selectionStart = editText.getSelectionStart();
            int selectionEnd = editText.getSelectionEnd();
            if (selectionStart > selectionEnd) {
                int tmp = selectionEnd;
                selectionEnd = selectionStart;
                selectionStart = tmp;
            }
            CharSequence selectedText = editText.getText().subSequence(selectionStart, selectionEnd);

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, selectedText.toString() /*deep copy*/); // sharing spanned string does not work here
            sendIntent.setType("text/plain");
            if (mShareActionProvider != null)
                mShareActionProvider.setShareIntent(sendIntent);
            return true;
        }
        return false;
    }

	@Override
	public void onDestroyActionMode(ActionMode arg0)
    {
        editText.setWindowFocusWait(false);
	}

}
