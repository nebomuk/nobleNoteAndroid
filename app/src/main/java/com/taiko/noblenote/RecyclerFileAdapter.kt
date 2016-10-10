package com.taiko.noblenote

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.recycler_file_item.view.*
import java.io.File
import java.io.FileFilter
import java.util.*

/**
 * Created by fabdeuch on 26.09.2016.
 */

class RecyclerFileAdapter(path: File, filter: FileFilter) : RecyclerView.Adapter<ViewHolder>() {

    private val mFiles: ArrayList<File>

    init {
        mFiles = FileSystemAdapter.listFiles(path, filter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_file_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        val vh = ViewHolder(view);
        vh.text1 = view.text1;
        return vh

    }

    fun getItem(pos : Int) : File = mFiles[pos]




    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text1.text = mFiles[position].name
    }



    override fun getItemCount(): Int {
        return mFiles.size
    }
}
