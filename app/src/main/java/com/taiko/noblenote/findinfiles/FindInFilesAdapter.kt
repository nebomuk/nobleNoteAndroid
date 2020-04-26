package com.taiko.noblenote.findinfiles

import android.annotation.SuppressLint
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.calculateDiff
import androidx.recyclerview.widget.RecyclerView
import com.taiko.noblenote.R
import com.taiko.noblenote.document.SFile
import kotlinx.android.synthetic.main.recycler_item_file.view.text1
import kotlinx.android.synthetic.main.recycler_item_find_in_files.view.*
import rx.lang.kotlin.PublishSubject
import rx.subjects.PublishSubject

class FindInFilesAdapter : RecyclerView.Adapter<ResultViewHolder>() {

    fun setItemSource(input : List<FindResult>)
    {
        val newItems = input.sortedBy { it.file.name };

        val diffResult = calculateDiff(CustomDiffCallback(this.items, newItems))

        this.items = newItems
        //notifyDataSetChanged();
        diffResult.dispatchUpdatesTo(this)
    }

    val clicks : PublishSubject<SFile> = PublishSubject();

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
        holder.itemView.setOnClickListener { clicks.onNext(items[holder.layoutPosition].file) }
    }


}

@BindingAdapter("itemSource")
fun <T> setRecyclerViewItemSoure(recyclerView: RecyclerView, items: List<FindResult>) {

    if (recyclerView.adapter is FindInFilesAdapter) {
        (recyclerView.adapter as FindInFilesAdapter).setItemSource(items)
    }
}

@BindingAdapter("onItemClick")
fun setRecyclerViewOnItemClick(recyclerView: RecyclerView, onItemClickListener : OnItemClickListener)
{
    if (recyclerView.adapter is FindInFilesAdapter) {
        (recyclerView.adapter as FindInFilesAdapter).clicks.subscribe { onItemClickListener.onItemClick(it) }

    }
}

interface OnItemClickListener {
    fun onItemClick(file: SFile);
}


class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{

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