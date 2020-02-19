package com.taiko.noblenote

import android.content.Context
import androidx.databinding.ObservableArrayList
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ViewSwitcher
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.view.longClicks
import kotlinx.android.synthetic.main.recycler_file_item.view.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.PublishSubject
import rx.lang.kotlin.plusAssign
import rx.subjects.PublishSubject
import java.text.Collator
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * file system adapter, can be configured inside onViewCreated or onCreate
 * but you must call refresh() in onStart/onResume
 */
class RecyclerFileAdapter(var path : SFile) : RecyclerView.Adapter<ViewHolder>() {

    private val log = loggerFor()

    private val mFiles: ObservableArrayList<FileItem> = ObservableArrayList()

    private val mClickSubject : PublishSubject<Int> = PublishSubject()
    private val mLongClickSubject : PublishSubject<Int> = PublishSubject()
    private val mSelectedFolderSubject : PublishSubject<Int> = PublishSubject();

    private val mWeakReferenceOnListChangedCallback: WeakReferenceOnListChangedCallback

    // initialized with a context in onAttachedToRecyclerView
    @ColorInt private var mSelectionColor: Int = 0

    @ColorInt private var mSelectedFolderColor: Int = 0; // selected folder highlight in two pane layout

    fun itemClicks(): Observable<Int> = mClickSubject.asObservable();

    fun itemLongClicks(): Observable<Int> = mLongClickSubject.asObservable();

    fun selectedFolder() : Observable<Int> = mSelectedFolderSubject.asObservable().distinctUntilChanged();

    fun itemCountChanged() : Observable<Int> = mFiles.toRxObservable().map { it.count() }

    val selectedFiles : List<SFile>
        get() = mFiles.filter { it.isSelected }.map { it.file }

    var showFolders = true;

/*    set(value) {
        mFiles.clear()
        mHandler.postDelayed({ mFiles.addAll(FileHelper.listFilesSorted(value, filter).map { FileItem(it,false) }) },0)
        field = value
    }*/

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
        // path = argPath
        mWeakReferenceOnListChangedCallback = WeakReferenceOnListChangedCallback(this);
        mFiles.addOnListChangedCallback(mWeakReferenceOnListChangedCallback)

        itemClicks().subscribe {
            if(selectFolderOnClick)
            {
                selectedFolderIndex = it;
            }
        }

        //rx.Observable.interval(3,TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { mFiles.add(File(Pref.rootPath,"test " + it)) }
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

    // reloads the file list by adding, removing files from the observable file list
    fun refresh(context: Context)
    {
        if(context == null)
        {
            log.w("RecyclerFileAdapter.refresh failed: argument context is null");
            return;
        }

            val newFileList = path.listFilesSorted(false);
            // add files that arent contained in the list
            for (newFile in newFileList) {
                if (!mFiles.any { it.file.name == newFile.name }) {
                    addFileName(newFile.name)
                }
            }
            // remove files
            val iter = mFiles.listIterator();
            while (iter.hasNext()) {
                val file = iter.next();
                if (!newFileList.any { it.name == file.file.name }) {
                    iter.remove();
                }
            }
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
        mSelectionColor = recyclerView.context.resources.getColor(R.color.md_grey_200)
        mSelectedFolderColor = recyclerView.context.resources.getColor(R.color.listHighlight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_file_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return ViewHolder(view)

    }

    fun removeSelected()
    {
        mFiles.removeAll { it.isSelected }

        updateFolderSelection()
    }

    fun addFileName(fileName : String)
    {
        mFiles.addSorted( FileItem(SFile(path,fileName),false), { lhs, rhs -> Collator.getInstance().compare(lhs.file.name, rhs.file.name) })

        updateFolderSelection()
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
        holder.itemView.text1.text = fileItem.file.name

        holder.mCompositeSubscription.clear() // clear subscriptions from previous bindings

        holder.mCompositeSubscription += holder.itemView.inner_layout
                .clicks()
                .doOnNext{ log.i("item click pos: " + position )}
                .subscribe { mClickSubject.onNext(holder.layoutPosition) }

        holder.mCompositeSubscription += holder.itemView.inner_layout
                .longClicks()
                .doOnNext{ log.i("item long click pos: " + position )}
                .subscribe { mLongClickSubject.onNext(holder.layoutPosition) }



        holder.itemView.outer_layout.setBackgroundColor(getBackgroundColor(position))

    }

    /**
     * @return the ColorInt background color depending on the selection state and folder clicked state (in two pane mode)
     */
    @ColorInt
    private fun getBackgroundColor(position : Int):  Int {
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

    private data class FileItem(val file : SFile,var isSelected: Boolean, var isSelectedFolder : Boolean = false) : Comparable<FileItem> {
        override fun compareTo(other: FileItem): Int {
            return this.file.name.compareTo(other.file.name) // this must also work for search results where the path is not the same
        }
    }

//    fun MutableList<File>.addSorted(mt : File)
//    {
//        var index = Collections.binarySearch<File>(this,mt, { lhs, rhs -> Collator.getInstance().compare(lhs.name, rhs.name) })
//        if (index < 0) index = index.inv()
//        add(index, mt)
//    }

    /**
     * based on
     * https://stackoverflow.com/questions/4903611/java-list-sorting-is-there-a-way-to-keep-a-list-permantly-sorted-automatically
     */
    fun <T> MutableList<T>.addSorted(mt : T, c : ((T, T) -> Int)) where T : Comparable<T>
    {
        var index = Collections.binarySearch<T>(this,mt, c)
        if (index < 0) index = index.inv()
        add(index, mt)
    }

    fun applyEmptyView(switcher : ViewSwitcher, @IdRes emptyViewId : Int, @IdRes  recyclerViewId : Int)
    {
        itemCountChanged()
                .throttleLast(250, TimeUnit.MILLISECONDS) // avoid interfering with the rv item animation
                .observeOn(AndroidSchedulers.mainThread()).subscribe {

            if(it > 0 && switcher.nextView.id == recyclerViewId) {
                switcher.showNext()
            }
            else if(it == 0 && switcher.nextView.id == emptyViewId)
            {
                switcher.showNext()
            }
        }
    }

}
