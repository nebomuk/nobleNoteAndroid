package com.taiko.noblenote

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.calculateDiff
import androidx.recyclerview.widget.RecyclerView
import com.taiko.noblenote.document.SFile
import kotlinx.android.synthetic.main.recycler_item_file.view.text1
import kotlinx.android.synthetic.main.recycler_item_find_in_files.view.*

class FindInFilesAdapter : RecyclerView.Adapter<ResultViewHolder>() {


    fun update(input : List<FindResult>)
    {
        val newItems = input.sortedBy { it.file.name };

        val diffResult = calculateDiff(CustomDiffCallback(this.items, newItems))

        this.items = newItems
        //notifyDataSetChanged();
        diffResult.dispatchUpdatesTo(this)
    }

    private var items : List<FindResult> = emptyList();

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {

        val itemContainer = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_item_find_in_files, parent, false)
        return ResultViewHolder(itemContainer);
    }

    override fun getItemCount(): Int {
        return items.size;
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val findResult = items[position]
        holder.itemView.text1.text = findResult.file.parentFile.name + " / " + findResult.file.name
        holder.itemView.text2.text = findResult.line;
    }


}

class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

}



class CustomDiffCallback(private val oldResults: List<FindResult>, private val newResults: List<FindResult>) : DiffUtil.Callback()
{
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldResults[oldItemPosition]?.file == newResults[newItemPosition]?.file;
    }

    override fun getOldListSize(): Int {
        return oldResults.size
    }

    override fun getNewListSize(): Int {
        return newResults.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldResults[oldItemPosition].file == newResults[newItemPosition].file;
    }

//    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
//        return if (oldResults[oldItemPosition].line != newResults[newItemPosition].line) {
//            java.lang.Boolean.FALSE
//        } else {
//            null
//        }
//    }

}