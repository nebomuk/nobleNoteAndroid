package com.taiko.noblenote;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileFilter;



public class NoteListFragment extends Fragment
{

    public static final String ARG_FOLDER_PATH = "folder_path";
    public static final int ITEM_NEW_NOTE = 1;
    
    
    private boolean mTwoPane = false;

    private String folderPath;
    
	private RecyclerFileAdapter fileSystemAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setHasOptionsMenu(true);


        if(getArguments().containsKey(MainActivity.ARG_TWO_PANE))
        {
            mTwoPane = getArguments().getBoolean(MainActivity.ARG_TWO_PANE);
        }
        if (getArguments().containsKey(ARG_FOLDER_PATH))
        {
            folderPath = getArguments().getString(ARG_FOLDER_PATH);
        }
        FileFilter fileFilter = new FileFilter(){
            public boolean accept(File pathname) {
                return pathname.isFile() && !pathname.isHidden();
            }};

        fileSystemAdapter = new RecyclerFileAdapter(new File(folderPath), fileFilter);

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

    public static void startNoteEditor(Context activity, File file)
    {
	    Intent detailIntent = new Intent(activity,NoteEditorActivity.class);
	    detailIntent.putExtra(NoteEditorActivity.ARG_FILE_PATH, file.getPath());
	    detailIntent.putExtra(NoteEditorActivity.ARG_OPEN_MODE, NoteEditorActivity.READ_WRITE);
	    activity.startActivity(detailIntent);
    }


}
