package com.taiko.noblenote;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import android.content.Intent;
import android.os.Bundle;

public class FolderListActivity extends SherlockFragmentActivity
        implements FolderListFragment.Callbacks {

    private boolean mTwoPane;
    public static final String ARG_TWO_PANE = "two_pane";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_list);
        
        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
            ((FolderListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }
    }

    public void onItemSelected(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(NoteListFragment.ARG_FOLDER_PATH, id);
            arguments.putBoolean(FolderListActivity.ARG_TWO_PANE, mTwoPane);
            NoteListFragment fragment = new NoteListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, NoteListActivity.class);
            detailIntent.putExtra(NoteListFragment.ARG_FOLDER_PATH, id);
            detailIntent.putExtra(FolderListActivity.ARG_TWO_PANE, mTwoPane);
            startActivity(detailIntent);
        }
    }
}
