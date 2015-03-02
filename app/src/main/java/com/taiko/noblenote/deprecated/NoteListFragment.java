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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * @author taiko
 *
 */
public class NoteListFragment extends SherlockListFragment {
		
		ArrayAdapter<String> adapter;
		String folderPath = "";
		private List<String> fileNameList = new ArrayList<String>();
		private OnNoteClickListener onNoteClickListener;		

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

		}
		
		// connect listener
		@Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        try {
	        	onNoteClickListener = (OnNoteClickListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString() + " must implement" + OnNoteClickListener.class.getName());
	        }
	    }
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			adapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1,fileNameList);
			setListAdapter(adapter);

		}
		
		/**
		 * set the path to the folder which contents will be displayed
		 * @param folderPath
		 */
		public void setPath(String folderPath)
		{
			this.folderPath = folderPath;
			
			if(folderPath == null)
				return;
			
			FileFilter fileFilter = new FileFilter(){
				public boolean accept(File pathname) {
					return pathname.isFile() && !pathname.isHidden();
				}};
				
			List<File> fileList = Arrays.asList(new File(folderPath).listFiles(fileFilter));
			ArrayList<String> fileNameListTemp = new ArrayList<String>(fileList.size());
			
			for(File file : fileList)
				fileNameListTemp.add(file.getName());
			
			Collections.sort(fileNameListTemp, Collator.getInstance()); // sort alphabetically
			
			fileNameList = fileNameListTemp;
			
			// in general, if you want to rename a file, simply remove and re-add it to tthe adapter
			
			// not necessary			
//			adapter.clear();
//			for(String name : fileNameList)
//				adapter.add(name);
//			adapter.notifyDataSetChanged();
			
//			getActivity().runOnUiThread(new Runnable() {
//			    public void run() {
//			        adapter.notifyDataSetChanged();
//			    }});
		}
		

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			String item = (String) getListAdapter().getItem(position);
			
			String filePath = folderPath + "/" + item.toString();
			onNoteClickListener.onNoteClick(filePath);
		}
		
		public interface OnNoteClickListener
		{
			public void onNoteClick(String filePath);
		}

	}
