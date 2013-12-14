/**
 * 
 */
package com.taiko.noblenote.deprecated;

import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.taiko.noblenote.CheckableListArrayAdapter;


/**
 * @author taiko
 * 
 * Activities must implement the OnFolderClickListener Interface
 *
 */
public class FolderListFragment extends SherlockListFragment 
{
	
	private String nnotesPath;
	OnFolderClickListener onFolderClickListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	// connect listener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	onFolderClickListener = (OnFolderClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement" +  OnFolderClickListener.class.getName());
        }
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		File path = Environment.getExternalStorageDirectory();
		nnotesPath = path + "/nnotes/";
		File dir = new File(nnotesPath);
		if(!dir.exists())
			dir.mkdir();
		
		// the following code lists only visible folders and push their names into an ArrayAdapter
		FileFilter folderFilter = new FileFilter(){
			public boolean accept(File pathname) {
				return pathname.isDirectory() && !pathname.isHidden();
			}};
			
		List<File> folderList = Arrays.asList(new File(nnotesPath).listFiles(folderFilter));
		List<String> folderNameList = new ArrayList<String>(folderList.size());
		
		for(File folder : folderList)
			folderNameList.add(folder.getName());
		
		Collections.sort(folderNameList, Collator.getInstance()); // sort alphabetically
		
		CheckableListArrayAdapter<String> adapter = new CheckableListArrayAdapter<String>(getActivity(), folderNameList);
		
		setListAdapter(adapter);

	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return container;
		//return inflater.inflate(R.layout.select_list_view,container);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) 
	{
		String item = (String) getListAdapter().getItem(position);
		onFolderClickListener.onFolderClick(nnotesPath + item);
	}
	
	public interface OnFolderClickListener
	{
		public void onFolderClick(String folderPath);
	}
};
