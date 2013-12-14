/**
 * 
 */
package com.taiko.noblenote.deprecated;

import java.io.File;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.taiko.noblenote.NoteEditorActivity;
import com.taiko.noblenote.deprecated.NoteListFragment.OnNoteClickListener;

/**
 * @author taiko
 *
 */
public class NoteListActivity extends SherlockFragmentActivity implements OnNoteClickListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Need to check if Activity has been switched to landscape mode
		// If yes, finished and go back to the start Activity
		if (getResources().getConfiguration().orientation == 
				Configuration.ORIENTATION_LANDSCAPE) {
			finish();
			return;
		}
//		setContentView(R.layout.note_list_activity);
		
		Bundle extras = getIntent().getExtras();
		String folderPath = extras.getString("folderPath");
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(new File(folderPath).getName());
		
//		NoteListFragment noteListFragment = (NoteListFragment) getSupportFragmentManager()
//				.findFragmentById(R.id.noteListFragment);
//		
//		if(noteListFragment != null)
//			noteListFragment.setPath(folderPath);
	
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // This is called when the Home (Up) button is pressed
	            // in the Action Bar.
	        	
	        	// docs say so but slower
	        	
//	            Intent parentActivityIntent = new Intent(this, MainActivity.class);
//	            parentActivityIntent.addFlags(
//	                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
//	                    Intent.FLAG_ACTIVITY_NEW_TASK);
//	            startActivity(parentActivityIntent);
	            finish();
	            return true;
	    }
	    return super.onOptionsItemSelected(item);
	}

	public void onNoteClick(String filePath) 
	{
		Intent intent = new Intent(this,NoteEditorActivity.class);
		intent.putExtra("filePath", filePath);
		startActivity(intent);
		
	}
}
