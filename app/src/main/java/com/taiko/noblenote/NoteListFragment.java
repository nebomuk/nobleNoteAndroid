package com.taiko.noblenote;

import java.io.File;
import java.io.FileFilter;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;



public class NoteListFragment extends ListFragment {

    public static final String ARG_FOLDER_PATH = "folder_path";
    public static final int ITEM_NEW_NOTE = 1;
    
    
    private boolean mTwoPane = false;

    private String folderPath;
    
    LongClickListener longClickListener = new LongClickListener(this);
	private FileSystemAdapter fileSystemAdapter;   


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setHasOptionsMenu(true);
    }
    
    @Override
    public void onResume ()
    {
    	super.onResume();
         if (getArguments().containsKey(ARG_FOLDER_PATH)) 
         {
             folderPath = getArguments().getString(ARG_FOLDER_PATH);
         }
         
         if(getArguments().containsKey(FolderListActivity.ARG_TWO_PANE))
         {
         	mTwoPane = getArguments().getBoolean(FolderListActivity.ARG_TWO_PANE);
         }
         
         FileFilter fileFilter = new FileFilter(){
 			public boolean accept(File pathname) {
 				return pathname.isFile() && !pathname.isHidden();
 			}};
         
 		fileSystemAdapter = new FileSystemAdapter(getActivity(),android.R.layout.simple_list_item_1, new File(folderPath), fileFilter);
         //CheckableListArrayAdapter<String> adapter = new CheckableListArrayAdapter<String>(getActivity(), fileNameList);
 		setListAdapter(fileSystemAdapter);	
    }
    
    @Override 
    public void onActivityCreated (Bundle savedInstanceState)
    {
    	super.onActivityCreated(savedInstanceState);
    	final ListView listView = this.getListView();
    	listView.setOnItemLongClickListener(longClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_note_list, container, false);
//        if (mItem != null) {
//            ((TextView) rootView.findViewById(R.id.item_detail)).setText(mItem.content);
//        }
        
        
        return rootView;
    }
    
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) 
    {
    	super.onListItemClick(listView, view, position, id);
    	if(!longClickListener.onListItemClick(listView, view, position, id)) // may consume the event if an CAB is active
    	{
    		File file = (File) fileSystemAdapter.getItem(position);
    		startNoteEditor(file);
    	}
    }
    
    public void startNoteEditor(File file)
    {
	    Intent detailIntent = new Intent(this.getActivity(),NoteEditorActivity.class);
	    detailIntent.putExtra(NoteEditorActivity.ARG_FILE_PATH, file.getPath());
	    detailIntent.putExtra(NoteEditorActivity.ARG_OPEN_MODE, NoteEditorActivity.READ_WRITE);
	    startActivity(detailIntent);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	longClickListener.onActivityResult(requestCode, resultCode, data);    	
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
    	menu.add(Menu.NONE,ITEM_NEW_NOTE,Menu.NONE,R.string.newNote).setIcon(mTwoPane ? R.drawable.ic_action_note_new : R.drawable.ic_action_new)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		//menu.add("Remove Note").setIcon(mTwoPane ? R.drawable.ic_action_remove : R.drawable.ic_action_remove)
		//.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    	
    }    
    
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
    	super.onOptionsItemSelected(item);
    	
    	if(item.getItemId() == ITEM_NEW_NOTE)
    	{
        	if(item.getItemId() == ITEM_NEW_NOTE)
        	{
        		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        		dialogBuilder.setTitle(R.string.newNote);
        		dialogBuilder.setMessage(R.string.enterName);

        		// Set an EditText view to get user input 
        		final EditText input = new EditText(this.getActivity());
        		input.setFilters(new InputFilter[]{new FileNameFilter()});
        		
        		// dont propose a name that already exists
        		String proposedFilePath = fileSystemAdapter.getRootDir().getPath() + File.separator + this.getString(R.string.newNote);
        		File proposed = new File(proposedFilePath);
        		int counter = 0;
        		while(proposed.exists())
        		{
        			proposed = new File(proposedFilePath + " (" + ++counter +")");
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
        						if(fileSystemAdapter.createFile(newFile) == 0)
        						{
        							startNoteEditor(newFile);    							        							
        						}        					
        						else // error occured
        						{
        							Toast.makeText(getActivity(), R.string.noteNotCreated, Toast.LENGTH_SHORT).show();
        						}
        					}
        				});
        		
        		// does nothing but creates a button
        		dialogBuilder.setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener()
    			{
    				public void onClick(DialogInterface dialog, int whichButton) 
    				{
    				}
    			});
        		
        		dialogBuilder.show();    		
        	}
    		
    	}
    	
		return false;
    }
}
