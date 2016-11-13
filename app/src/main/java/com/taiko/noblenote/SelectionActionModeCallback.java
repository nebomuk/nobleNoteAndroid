package com.taiko.noblenote;


import android.annotation.TargetApi;
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


/**
 * 
 * @author Taiko-G780
 *
 * a selection contextual action mode with cut, copy and paste and additional formatting buttons
 * for Honeycomb and newer devices *
 */


@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
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
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        return true;
    }
	@Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

        editText.setWindowFocusWait(true);
        return false;
    }
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        switch(item.getItemId()) {

            case R.id.bold: {
                Format.toggleStyleSpan(this.editText, Typeface.BOLD);
                editText.setModified(true); // apparently doesnt get set automatically by DroidWriterEditText's TextWatcher
                return true;
            }
            case R.id.italic: {
                Format.toggleStyleSpan(this.editText, Typeface.ITALIC);
                editText.setModified(true);
                return true;
            }
            case R.id.underline: {
                Format.toggleCharacterStyle(this.editText, UnderlineSpan.class);
                editText.setModified(true);
                return true;
            }
            case R.id.strikethrough: {
                Format.toggleCharacterStyle(this.editText, StrikethroughSpan.class);
                editText.setModified(true);
                return true;
            }
            case R.id.menu_item_share: {
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

        }
        return false;
    }

	@Override
	public void onDestroyActionMode(ActionMode arg0)
    {
        editText.setWindowFocusWait(false);
	}

}
