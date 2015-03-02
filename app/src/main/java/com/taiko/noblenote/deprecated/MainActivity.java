package com.taiko.noblenote.deprecated;


import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;


public class MainActivity extends SherlockFragmentActivity implements FolderListFragment.OnFolderClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(SampleList.THEME); //Used for theme switching in samples
        super.onCreate(savedInstanceState);
        
        //setContentView(R.layout.main);
        
//        getSupportActionBar().setTitle(R.string.notebooks);
    }

	public void onFolderClick(String folderPath) 
	{
////		NoteListFragment noteListFragment = (NoteListFragment) getSupportFragmentManager()
////				.findFragmentById(R.id.noteListFragment);
//		
//		if(noteListFragment != null && noteListFragment.isInLayout())
//			noteListFragment.setPath(folderPath);	
//		else
//		{
//			// A NoteListFragment is not in the layout (handset layout),
//            // so start NoteListActivity
//            // and pass it the info about the selected item
//            Intent intent = new Intent(this, NoteListActivity.class);
//            intent.putExtra("folderPath", folderPath);
//            startActivity(intent);		
//		}
	}
}
