package com.taiko.noblenote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.devpaul.filepickerlibrary.FilePickerActivity;
import com.devpaul.filepickerlibrary.enums.ThemeType;

import java.io.File;
import java.io.FileFilter;


public class FolderListFragment extends ListFragment {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    public static final int ITEM_NEW_FOLDER = 2;
    public static final String ROOT_PATH = "root_path";

    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private boolean mTwoPane = false;

	private String rootPath;

    public interface Callbacks {

        public void onItemSelected(String id);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        public void onItemSelected(String id) {
        }
    };
	private FileSystemAdapter fileSystemAdapter;

    public FolderListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
       // this.setHasOptionsMenu(true);

//        FloatingActionButton fab = (FloatingActionButton)getActivity().findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new MaterialDialog.Builder(FolderListFragment.this.getActivity())
//                        .positiveText(R.string.newNote).negativeText(R.string.newNotebook).callback(
//                        new MaterialDialog.ButtonCallback() {
//                            @Override
//                            public void onPositive(MaterialDialog dialog) {
//                                super.onPositive(dialog);
//                            }
//
//                            @Override
//                            public void onNegative(MaterialDialog dialog) {
//                                super.onNegative(dialog);
//                            }
//                        }
//                );
//            }
//        });

    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        // show an input dialog where the user can enter the desired root path if it has not been set before
        if (!prefs.contains(ROOT_PATH)) {

            Intent filePickerIntent = new Intent(this.getActivity(), FilePickerActivity.class);
            filePickerIntent.putExtra(FilePickerActivity.THEME_TYPE, ThemeType.DIALOG);
            filePickerIntent.putExtra(FilePickerActivity.REQUEST_CODE, FilePickerActivity.REQUEST_DIRECTORY);
            startActivityForResult(filePickerIntent, FilePickerActivity.REQUEST_DIRECTORY);
        }

        String fallbackRootPath = prefs.getString(ROOT_PATH, Environment.getExternalStorageDirectory().getAbsolutePath() + "/nobleNote");

        // always try to either read from the prefs or set a default path
        rootPath = prefs.getString(ROOT_PATH,fallbackRootPath);
        if(fileSystemAdapter == null)
            initFolders();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == FilePickerActivity.REQUEST_DIRECTORY) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            final SharedPreferences.Editor prefEditor = prefs.edit();


            if (resultCode == FilePickerActivity.RESULT_OK) {

                String filePath = data.
                        getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
                if (filePath != null) {
                    rootPath = filePath;
                }
            }

            prefEditor.putString(ROOT_PATH, rootPath);
            prefEditor.commit();  // save the preference
            initFolders();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    
    /**
     * shows the folders on the screen
     */
    public void initFolders()
    {
    	File dir = new File(rootPath);
		if(!dir.exists())
			dir.mkdirs();
		
		// the following code lists only visible folders and push their names into an ArrayAdapter
		FileFilter folderFilter = new FileFilter(){
			public boolean accept(File pathname) {
				return pathname.isDirectory() && !pathname.isHidden();
			}};
				
		fileSystemAdapter = new FileSystemAdapter(getActivity(),android.R.layout.simple_list_item_1, new File(rootPath),folderFilter);
		
		setListAdapter(fileSystemAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState
                .containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        File folder = (File)getListAdapter().getItem(position);
        mCallbacks.onItemSelected(folder.getPath());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
    	 if (this.getActivity().findViewById(R.id.item_detail_container) != null)
             mTwoPane = true;
    	 
    	menu.add(Menu.NONE,ITEM_NEW_FOLDER,Menu.NONE,R.string.newNotebook).setIcon(mTwoPane ? R.drawable.ic_action_notebook_new : R.drawable.ic_action_new)
    	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    	
    	  	
    }
    
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
    	super.onOptionsItemSelected(item);
    	
    	if(item.getItemId() == ITEM_NEW_FOLDER)
    	{
    		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

    		dialogBuilder.setTitle(R.string.newNotebook);
    		dialogBuilder.setMessage(R.string.enterName);

    		// Set an EditText view to get user input 
    		final EditText input = new EditText(this.getActivity());
    		input.setFilters(new InputFilter[]{new FileNameFilter()});
    		
    		String proposedDirPath = rootPath + File.separator + this.getString(R.string.newNotebook);
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
    						if(fileSystemAdapter.mkdir(new File(rootPath + File.separator + newName)) != 0)
    						{
    							Toast.makeText(getActivity(), R.string.notebookNotCreated, Toast.LENGTH_SHORT).show();
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
    	
		return false;
    }
}
