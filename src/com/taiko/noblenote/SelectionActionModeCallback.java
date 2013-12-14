package com.taiko.noblenote;


import android.annotation.TargetApi;
import android.graphics.Typeface;
import android.os.Build;
import android.text.style.StrikethroughSpan;
import android.text.style.UnderlineSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


/**
 * 
 * @author Taiko-G780
 *
 * a selection contextual action mode with cut, copy and paste and additional formatting buttons
 * for Honeycomb and newer devices *
 */


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SelectionActionModeCallback implements android.view.ActionMode.Callback {

	
	private DroidWriterEditText editText;
	
    public SelectionActionModeCallback(DroidWriterEditText editText) {
		super();
		this.editText = editText;
	}
    
    @Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.selection_cab, menu);
        menu.removeItem(android.R.id.selectAll);
        return true;
    }
	@Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        switch(item.getItemId()) {

        case R.id.bold: 
        {
        	Format.toggleStyleSpan(this.editText,Typeface.BOLD);
        	editText.setModified(true); // apparently doesnt get set automatically by DroidWriterEditText's TextWatcher
            return true;
        }
		case R.id.italic:
        {
        	Format.toggleStyleSpan(this.editText,Typeface.ITALIC);
        	editText.setModified(true);
            return true;
        }
        case R.id.underline:
        {
        	Format.toggleCharacterStyle(this.editText,UnderlineSpan.class);
        	editText.setModified(true);
            return true;
        }
        case R.id.strikethrough:
        {
        	Format.toggleCharacterStyle(this.editText,StrikethroughSpan.class);
        	editText.setModified(true);
        	return true;
        }
        
        }  
        return false;
    }

	@Override
	public void onDestroyActionMode(ActionMode arg0) {		
	}
	

}
