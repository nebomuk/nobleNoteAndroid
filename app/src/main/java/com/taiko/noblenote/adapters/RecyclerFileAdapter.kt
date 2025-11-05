package com.taiko.noblenote.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.view.longClicks
import com.taiko.noblenote.R
import com.taiko.noblenote.databinding.RecyclerItemFileBinding
import com.taiko.noblenote.extensions.getColorFromAttr
import com.taiko.noblenote.extensions.toRxObservable
import com.taiko.noblenote.filesystem.SFile
import com.taiko.noblenote.util.loggerFor
import com.taiko.noblenote.BR
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.PublishSubject
import rx.lang.kotlin.plusAssign
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit


/**
 * file system adapter, can be configured inside onViewCreated or onCreate
 * but you must call refresh() in onStart/onResume
 */

data class FileItem(val file : SFile, var isSelected: Boolean, var isSelectedFolder : Boolean = false) : Comparable<FileItem> {
    override fun compareTo(other: FileItem): Int {
        return this.file.name.compareTo(other.file.name) // this must also work for search results where the path is not the same
    }
}

class RecyclerFileAdapter(var path : SFile) : RecyclerView.Adapter<ViewHolder>() {

    private val log = loggerFor()

    private val mFiles: ObservableArrayList<FileItem> = ObservableArrayList()

    private val mClickSubject : PublishSubject<Int> = PublishSubject()
    private val mLongClickSubject : PublishSubject<Int> = PublishSubject()
    private val mSelectedFolderSubject : PublishSubject<Int> = PublishSubject();

    // initialized with a context in onAttachedToRecyclerView
    @ColorInt private var mSelectionColor: Int = 0

    @ColorInt private var mSelectedFolderColor: Int = 0; // selected folder highlight in two pane layout

    fun itemClicks(): Observable<Int> = mClickSubject.asObservable();

    fun itemLongClicks(): Observable<Int> = mLongClickSubject.asObservable();

    fun selectedFolder() : Observable<Int> = mSelectedFolderSubject.asObservable().distinctUntilChanged();

    private fun itemCountChanged() : Observable<Int> = mFiles.toRxObservable().map { it.count() }

    val selectedFiles : List<SFile>
        get() = mFiles.filter { it.isSelected }.map { it.file }

    var showFolders = true;


    var selectFolderOnClick : Boolean = false
    set(value) {
        if(value == field)
        {
            return;
        }
        field = value;

        if(field && selectedFolderIndex == RecyclerView.NO_POSITION)
        {
            selectedFolderIndex = 0; // try to select first if any
        }
    }

    init {

        itemClicks().subscribe {
            if(selectFolderOnClick)
            {
                selectedFolderIndex = it;
            }
        }
    }

    private fun setItemSource(input : List<SFile>)
    {
        val newItems = input.map { FileItem(it,false,false) };

        val diffResult = DiffUtil.calculateDiff(FileItemDiffCallback(this.mFiles, newItems))

        this.mFiles.clear();
        this.mFiles.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }


    // selected folder in two-pane layout, selects the given index
    private var selectedFolderIndex: Int = RecyclerView.NO_POSITION
        set(value) {
            if(!isValidIndex(value))
            {
                field = RecyclerView.NO_POSITION;
            }

            else
            {
                field = value;

                val indexOfOldValue = mFiles.indexOfFirst { it.isSelectedFolder }
                if(indexOfOldValue != -1)
                {
                mFiles[indexOfOldValue].isSelectedFolder = false;
                notifyItemChanged(indexOfOldValue);
                }
                mFiles[field].isSelectedFolder = true;
                notifyItemChanged(field);

            }

            mSelectedFolderSubject.onNext(field);

        }
    get() =  mFiles.indexOfFirst { it.isSelectedFolder }

    fun refresh()
    {
        val newFileList = path.listFilesSorted(false).filter { !it.name.startsWith(".") };
        setItemSource(newFileList);

        updateFolderSelection()
    }

    // when folder selection is enabled, selects at least one folder
    // or sends a -1 RecyclerView.NO_POSITION when  mFiles is empty
    private fun updateFolderSelection()
    {
        if(selectFolderOnClick) {
            if (selectedFolderIndex == RecyclerView.NO_POSITION && mFiles.size > 0) {
                selectedFolderIndex = 0; // try select first
            }
            else if(mFiles.size == 0)
            {
                selectedFolderIndex = RecyclerView.NO_POSITION;
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
     mSelectedFolderColor = ColorUtils.setAlphaComponent(recyclerView.context.getColorFromAttr(
         androidx.navigation.ui.R.attr.colorSecondary),128);
        mSelectionColor = recyclerView.context.getColorFromAttr(androidx.navigation.ui.R.attr.colorSecondary);
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Use the generated binding class to inflate the layout
        val binding = RecyclerItemFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        // The rest of the logic can stay for now, but note how iconImageView is accessed via binding
        if (!showFolders) {
            binding.iconImageView.setImageResource(R.drawable.ic_event_note_black_24dp)
        }

        // Return the new ViewHolder holding the binding object
        return ViewHolder(binding)
    }

    fun getItem(pos : Int) : SFile? {
        if(isValidIndex(pos))
        {
            return mFiles[pos].file
        }
        else
            return null;
    }

    fun setSelected(pos : Int, isSelected: Boolean)
    {
        if(!isValidIndex(pos))
            return;

        if( mFiles[pos].isSelected == isSelected)
            return;

        mFiles[pos].isSelected = isSelected;
        notifyItemChanged(pos)
    }

    fun isSelected(pos : Int): Boolean {
        if(!isValidIndex(pos))
            return false;

        return mFiles[pos].isSelected;
    }

    fun clearSelection()
    {
        for (i in 0.. mFiles.size-1)
        {
            mFiles[i].isSelected = false;
            notifyItemChanged(i)
        }
    }

    private fun isValidIndex(pos : Int) : Boolean
    {
        return pos >= 0 && pos <= mFiles.size -1;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileItem = mFiles[position]

        // 1. Set the variables defined in the XML <data> block
        // This replaces holder.itemView.text1.text = fileItem.file.name
        holder.binding.setVariable(BR.fileItem, fileItem)

        // Also set the 'adapter' and 'position' variables needed for the background color binding expression
        holder.binding.setVariable(BR.adapter, this) // 'this' refers to RecyclerFileAdapter
        holder.binding.setVariable(BR.position, position)

        // Execute pending bindings immediately (important for RecyclerView performance)
        holder.binding.executePendingBindings()

        // 2. The RxJava click handling can now use the binding root view directly
        holder.mCompositeSubscription.clear() // clear subscriptions from previous bindings

        // outer_layout is accessed via binding.outerLayout
        holder.mCompositeSubscription += holder.binding.root.findViewById< View>(R.id.outer_layout)
            .clicks()
            .doOnNext { log.i("item click pos: " + position) }
            .subscribe { mClickSubject.onNext(holder.layoutPosition) }

        holder.mCompositeSubscription += holder.binding.root.findViewById< View>(R.id.outer_layout)
            .longClicks()
            .doOnNext { log.i("item long click pos: " + position) }
            .subscribe { mLongClickSubject.onNext(holder.layoutPosition) }

        // 3. The manual call to setBackgroundColor is no longer needed
        // holder.itemView.outer_layout.setBackgroundColor(getBackgroundColor(position))
        // This is handled by the data binding expression in the XML: android:backgroundTint="@{adapter.getBackgroundColor(position)}"
    }

    /**
     * @return the ColorInt background color depending on the selection state and folder clicked state (in two pane mode)
     */
    @ColorInt
    public fun getBackgroundColor(position : Int):  Int {
        var backgroundColor = Color.TRANSPARENT;
        val fileItem = mFiles[position]
        if(fileItem.isSelected && fileItem.isSelectedFolder)
        {
            val alphaed = ColorUtils.setAlphaComponent(mSelectionColor,150);
            backgroundColor = ColorUtils.compositeColors(alphaed, mSelectedFolderColor);
        }
        else if(fileItem.isSelected)
        {
            backgroundColor = mSelectionColor
        }
        else if(fileItem.isSelectedFolder)
        {
            backgroundColor = mSelectedFolderColor
        }
        return backgroundColor;
    }

    override fun getItemCount(): Int {
        return mFiles.size
    }

    @CheckResult
    fun applyEmptyView(switcher : ViewSwitcher, @IdRes emptyViewId : Int, @IdRes  recyclerViewId : Int): Subscription {
        return itemCountChanged()
                .throttleLast(250, TimeUnit.MILLISECONDS) // avoid interfering with the rv item animation
                .observeOn(AndroidSchedulers.mainThread()).subscribe {

            if(it > 0 && switcher.nextView.id == recyclerViewId) {
                switcher.showNext()
            }
            else if(it == 0 && switcher.nextView.id == emptyViewId)
            {
                switcher.showNext()

                if(showFolders)
                {
                    switcher.findViewById<TextView>(R.id.tv_recycler_view_empty).setText(R.string.notebook_list_empty);
                }
                else
                {
                    switcher.findViewById<TextView>(R.id.tv_recycler_view_empty).setText(R.string.note_list_empty);
                }
            }
        }
    }

}

class FileItemDiffCallback(private val oldResults: List<FileItem>, private val newResults: List<FileItem>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldResults[oldItemPosition]?.file == newResults[newItemPosition]?.file
        ;
    }

    override fun getOldListSize(): Int {
        return oldResults.size
    }

    override fun getNewListSize(): Int {
        return newResults.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldResults[oldItemPosition].file == newResults[newItemPosition].file
                &&
                oldResults[oldItemPosition].isSelectedFolder == newResults[newItemPosition].isSelectedFolder
        ;
    }
}
