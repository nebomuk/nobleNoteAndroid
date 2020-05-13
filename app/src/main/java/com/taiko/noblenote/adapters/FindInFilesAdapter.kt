package com.taiko.noblenote.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.toSpannable
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.calculateDiff
import androidx.recyclerview.widget.RecyclerView
import com.taiko.noblenote.R
import com.taiko.noblenote.filesystem.SFile
import com.taiko.noblenote.extensions.indicesOf
import com.taiko.noblenote.filesystem.FindResult
import com.taiko.noblenote.util.loggerFor
import kotlinx.android.synthetic.main.recycler_item_file.view.text1
import kotlinx.android.synthetic.main.recycler_item_find_in_files.view.*
import rx.lang.kotlin.PublishSubject
import rx.subjects.PublishSubject


class FindInFilesAdapter : RecyclerView.Adapter<ResultViewHolder>() {

    private val log = loggerFor()

    fun setItemSource(input : List<FindResult>)
    {
        val newItems = input.sortedBy { it.file.name };

        val diffResult = calculateDiff(CustomDiffCallback(this.items, newItems))

        this.items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    val clicks : PublishSubject<FindResult> = PublishSubject();

    private var items : List<FindResult> = emptyList();

    var queryText : String = "";

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
        holder.itemView.text1.setText(highlightQuery(findResult.file.parentFile.name + " / " + findResult.file.name), TextView.BufferType.SPANNABLE)
        holder.itemView.text2.setText(highlightQuery(findResult.line),TextView.BufferType.SPANNABLE);
        holder.itemView.setOnClickListener { clicks.onNext(items[holder.layoutPosition]) }
    }

    private fun highlightQuery(sourceText : CharSequence) : CharSequence {
        if(queryText.isBlank())
        {
            return sourceText;
        }

        val indicesOfOccurrences = sourceText.indicesOf(queryText)

        if(indicesOfOccurrences.isEmpty())
        {
            return sourceText;
        }



        val spannableString = SpannableString(sourceText)


        try {
            for (index in indicesOfOccurrences) {

                val end = index + queryText.length;

                spannableString.setSpan(ForegroundColorSpan(Color.BLUE), index, end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        } catch (e: IndexOutOfBoundsException) {
            log.w("failed to highlight find result: index out of bounds")
            return spannableString;
        }

        val threshold = 20
        if(indicesOfOccurrences.first() > threshold && spannableString.length > (threshold + queryText.length) + 5)
        {
            val builder = SpannableStringBuilder("");
            builder.append("…");
            val truncated =  spannableString.subSequence(indicesOfOccurrences.first() - threshold,spannableString.length );
            builder.append(truncated);
            return builder.toSpannable();

        }
        return spannableString;

    }
}



@BindingAdapter("queryText")
fun <T> setRecyclerViewQueryText(recyclerView: RecyclerView, queryText : String) {

    if (recyclerView.adapter is FindInFilesAdapter) {
       (recyclerView.adapter as FindInFilesAdapter).queryText = queryText;
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
    fun onItemClick(file: FindResult);
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