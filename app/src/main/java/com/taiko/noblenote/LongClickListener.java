package com.taiko.noblenote;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Taiko-G780
 *
 */
public class LongClickListener implements ListView.OnItemLongClickListener
{
	private ActionMode mActionMode;
    private SelectionModel selectionModel = new SelectionModel();
    private ListFragment fragment;
    private MenuItem renameMenuItem;
    private MenuItem showSourceMenuItem;
    
    public static final int NEW_FILE_NAME = 1;
    
    /**
     * consume list clicks if the CAB is active
     * @param listView
     * @param view
     * @param position
     * @param id
     * @return true if CAB is active
     */
    public boolean onListItemClick(ListView listView, View view, int position, long id)
    {
    	if(mActionMode != null)
    	{
    		selectionModel.toggleSelection(position, view);
    		return true;
    	}
    	else
    		return false;
    }

    
    /**
     * expects the fragment's listAdapter to be a FileSystemAdapter
     * @param fragment
     */
	public LongClickListener(ListFragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view,
			int pos, long id) {
		 if (mActionMode != null) {
			 mActionMode.finish();
	            return true;
	        }

	        // Start the CAB using the ActionMode.Callback defined abov
		 
		 
	        mActionMode = fragment.getActivity().startActionMode(new ActionModeCallback());
	        selectionModel.toggleSelection(pos, view);
	        //view.setSelected(true);
	        //listView.setItemChecked(pos, true);
	        
	        //listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
	        
	        return true;
	}
	
	private class ActionModeCallback implements ActionMode.Callback {

        

		// Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        	
        	renameMenuItem = menu.findItem(R.id.actionRename);
        	
        	showSourceMenuItem = menu.findItem(R.id.actionShowHtml);
        	
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.note_list_cab, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) 
        {
        	int id = item.getItemId();
        	switch(id)
        	{
        	case R.id.actionRename: // TODO try out  AlertDialog.Builder
        	{
        		// TODO keep old rename dialog as fallback for android  2.3?
        		
        		// get first isSelected item, this is a File
        		Object noteItem = fragment.getListAdapter().getItem(selectionModel.getSelectionMap().keySet().iterator().next()); // get first key
        		
        		if(noteItem == null)
        			return false;
        		
        		final File noteFile = (File)noteItem;
//    		    Intent intent = new Intent(fragment.getActivity(),RenameDialogActivity.class);
//    		    intent.putExtra(RenameDialogActivity.ARG_FILE_NAME, ((File)noteItem).getName());
//    		    fragment.startActivityForResult(intent, NEW_FILE_NAME); 
       
        		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(fragment.getActivity());

        		dialogBuilder.setTitle(R.string.renameNote);
        		dialogBuilder.setMessage(R.string.enterNewName);

        		// Set an EditText view to get user input 
        		final EditText input = new EditText(fragment.getActivity());
        		input.setFilters(new InputFilter[]{new FileNameFilter()});
        		input.setText(noteFile.getName());
        		input.setSelection(input.getText().length());
        		dialogBuilder.setView(input);
        		
        		dialogBuilder.setPositiveButton(
        				android.R.string.ok, new DialogInterface.OnClickListener()
        				{
        					public void onClick(DialogInterface dialog, int whichButton) 
        					{
        						Editable newName = input.getText();

        						final FileSystemAdapter adapter = ((FileSystemAdapter)fragment.getListAdapter());

        						switch(adapter.rename(noteFile, newName.toString()))
        						{
        						case FileSystemAdapter.FILE_EXISTS:
        						case FileSystemAdapter.NAME_EMPTY:
        						case FileSystemAdapter.FAILED:
        						{
        							Toast.makeText(fragment.getActivity(), R.string.noteNotRenamed, Toast.LENGTH_SHORT).show();	
        							break;
        						}
        						}

        						// always finish CAB after rename dialog
        						if (mActionMode != null) 
        						{
        							mActionMode.finish();
        						}	
        					}
        				});
        		dialogBuilder.setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener()
        				{
							public void onClick(DialogInterface dialog, int whichButton) {
		    			    		if (mActionMode != null) 
		    			    		{
		    			    			 mActionMode.finish();
		    			    	    }	
								}
        				});

        		dialogBuilder.show();
        		
        		return true;
        	}
        	case R.id.removeNote:
        	{       
        		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(fragment.getActivity());

        		dialogBuilder.setTitle(R.string.removeMultipleNotes);
        		dialogBuilder.setMessage(R.string.removeMultipleNotesQuestion);
        		
        		final ArrayList<File> files = new ArrayList<File>();
        		Iterator<Integer> it = selectionModel.getSelectionMap().keySet().iterator();
        		while(it.hasNext())
        		{
        			File file = (File) fragment.getListAdapter().getItem(it.next());
        			if(file != null)
        				files.add(file);
        		}
        		
        		dialogBuilder.setPositiveButton(android.R.string.ok, 
        				new DialogInterface.OnClickListener()
        		{
        					public void onClick(DialogInterface dialog, int whichButton) 
        					{
        						final FileSystemAdapter adapter = ((FileSystemAdapter)fragment.getListAdapter());

        						// files must be listed separately first because the internal file list (accessed with getItem) of FileSystemAdapter
        						// is invalidated if a file is removed        						
        						for(File file : files)
        						{
        		        				if(adapter.removeFile(file) != 0)
        		        				{
        		        					Toast.makeText(fragment.getActivity(), R.string.notesNotRemoved, Toast.LENGTH_SHORT).show();	
        		        				}
        						}

        						// always finish CAB after rename dialog
        						if (mActionMode != null) 
        						{
        							mActionMode.finish();
        						}
        					}
        		});
        		
        		
        		dialogBuilder.setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener()
        				{
							public void onClick(DialogInterface dialog, int whichButton) {
		    			    		if (mActionMode != null) 
		    			    		{
		    			    			 mActionMode.finish();
		    			    	    }	
								}
        				});

        		dialogBuilder.show();
        		
        		return true;
        		
        	}
        	case R.id.actionShowHtml:
        	{
        		
        		Object noteItem = fragment.getListAdapter().getItem(selectionModel.getSelectionMap().keySet().iterator().next());
        		
        		if(noteItem == null)
        			return false;
        		
    		    Intent intent = new Intent(fragment.getActivity(),NoteEditorActivity.class);
    		    intent.putExtra(NoteEditorActivity.Companion.getARG_FILE_PATH(), ((File)noteItem).getPath());
    		    intent.putExtra(NoteEditorActivity.Companion.getARG_OPEN_MODE(), NoteEditorActivity.Companion.getHTML());
    		    fragment.startActivity(intent);
    		    mode.finish(); // Action picked, so close the CAB
    		    return true;
        	}
        	default:
        	{
        		return false;
        	}
        	}
//            switch (item.getItemId()) {
////                case R.id.menu_share:
////                    shareCurrentItem();
////                    mode.finish(); 
////                    return true;
//                default:
//                    return false;
//            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            selectionModel.clearSelection();
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if(requestCode == NEW_FILE_NAME)
    	{
    		if(resultCode == Activity.RESULT_OK)
    		{
	    		String newName = data.getStringExtra(RenameDialogActivity.ARG_FILE_NAME);
	    		
	    		// get first isSelected item, this is a File
	    		Object noteItem = fragment.getListAdapter().getItem(selectionModel.getSelectionMap().keySet().iterator().next());
	    		
    		}
    		
    		// always finish CAB after rename dialog
    		if (mActionMode != null) 
    		{
    			 mActionMode.finish();
    	    }		
    	}
    	
    }
	
    // Todo do non-stable ids affect this
	@SuppressWarnings("deprecation")
    class SelectionModel
    {
    	private Drawable background;
    	@SuppressLint("UseSparseArrays") // because there's no entrySet() method in SparseArray
		private HashMap<Integer,View> selectionMap = new HashMap<Integer,View>();


    	public void toggleSelection(int position, View view)
    	{
    		if(selectionMap.get(position) == null)
    		{
    			if(background == null) // assume all items have the same background
    				background = view.getBackground();

    			view.setBackgroundColor(Color.BLUE);
    			selectionMap.put(position, view);
    		}
    		else
    		{
    			view.setBackgroundDrawable(background);
    			selectionMap.remove(position);
    		}	
    		
    		if(renameMenuItem != null) // only if exactly one item is isSelected, the rename menu item should be available
    		{
    			renameMenuItem.setVisible(selectionMap.size() == 1);
    			renameMenuItem.setEnabled(selectionMap.size() == 1); // disables shortcuts
    		}
    		
    		if(showSourceMenuItem != null) // only if exactly one item is isSelected, the rename menu item should be available
    		{
    			showSourceMenuItem.setVisible(selectionMap.size() == 1);
    			showSourceMenuItem.setEnabled(selectionMap.size() == 1); // disables shortcuts
    		}
    	}

    	public void clearSelection()
    	{
    		Iterator<View> it = selectionMap.values().iterator();
    		while(it.hasNext())
    		{
    			View view = it.next();
    			view.setBackgroundDrawable(background);
    		}
    		selectionMap.clear();
    	}

		public HashMap<Integer,View> getSelectionMap() {
			return selectionMap;
		}
    }
}
