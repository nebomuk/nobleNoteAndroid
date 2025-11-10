package de.blogspot.noblenoteandroid.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.toSpannable
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.calculateDiff
import androidx.recyclerview.widget.RecyclerView
import de.blogspot.noblenoteandroid.databinding.RecyclerItemFindInFilesBinding
import de.blogspot.noblenoteandroid.extensions.indicesOf
import de.blogspot.noblenoteandroid.filesystem.FindResult
import de.blogspot.noblenoteandroid.util.loggerFor
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
        // Use the generated binding class's inflate method
        val binding = RecyclerItemFindInFilesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResultViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size;
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val findResult = items[position]

        // Access views directly via the binding object held in the ViewHolder
        holder.binding.text1.setText(
            highlightQuery(findResult.file.parentFile.name + " / " + findResult.file.name),
            TextView.BufferType.SPANNABLE
        )
        holder.binding.text2.setText(
            highlightQuery(findResult.line),
            TextView.BufferType.SPANNABLE
        )
        // Set the click listener on the root view of the binding
        holder.binding.root.setOnClickListener { clicks.onNext(items[holder.layoutPosition]) }
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


class ResultViewHolder(val binding: RecyclerItemFindInFilesBinding) :
    RecyclerView.ViewHolder(binding.root)
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