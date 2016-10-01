package com.taiko.noblenote;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * Created by fabdeuch on 26.09.2016.
 */

public class RecyclerFileAdapter extends RecyclerView.Adapter<ViewHolder>
{

    private final ArrayList<File> mFiles;

    public RecyclerFileAdapter(File path, FileFilter filter)
    {
        mFiles = FileSystemAdapter.listFiles(path, filter);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_file_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        vh.text1 = (TextView) v.findViewById(R.id.text1);
        return vh;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        holder.text1.setText(mFiles.get(position).getName());
    }

    @Override
    public int getItemCount()
    {
        return mFiles.size();
    }
}
