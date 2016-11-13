package com.taiko.noblenote

import android.databinding.ObservableArrayList
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.recycler_file_item.view.*
import java.io.File
import java.io.FileFilter
import java.text.Collator
import java.util.*

/**
 * Created by fabdeuch on 26.09.2016.
 */

class RecyclerFileAdapter(path: File, filter: FileFilter) : RecyclerView.Adapter<ViewHolder>() {

    private val mFiles: ObservableArrayList<File>

    private val mWeakReferenceOnListChangedCallback: WeakReferenceOnListChangedCallback

    init {
        mFiles = ObservableArrayList()
        mWeakReferenceOnListChangedCallback = WeakReferenceOnListChangedCallback(this);
        mFiles.addOnListChangedCallback(mWeakReferenceOnListChangedCallback)
        val h = Handler()
        h.postDelayed({ mFiles.addAll(listFiles(path, filter)) },0)

        // TODO use binary search to insert so that list stays sorted
        //rx.Observable.interval(3,TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { mFiles.add(File(Pref.rootPath,"test " + it)) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_file_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        val vh = ViewHolder(view)
        vh.text1 = view.text1

        return vh

    }

    fun addFile(f : File)
    {
        mFiles.addSorted(f, { lhs, rhs -> Collator.getInstance().compare(lhs.name, rhs.name) })
    }

    fun getItem(pos : Int) : File = mFiles[pos]




    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text1.text = mFiles[position].name
    }



    override fun getItemCount(): Int {
        return mFiles.size
    }

    companion object
    {
        /**
         * creates a writable list of the contents of the directory
         */
        @JvmStatic
        fun listFiles(dir: File, filter: FileFilter): ArrayList<File> {
            //List<File> fileList = Arrays.asList(); // returns read only list, causes unsupported operation exceptions in adapter
            val fileList = ArrayList<File>()
            if (dir.exists() || dir.mkdirs()) {
                Collections.addAll(fileList, *dir.listFiles(filter))
                Collections.sort(fileList) { lhs, rhs -> Collator.getInstance().compare(lhs.name, rhs.name) }
            }
            return fileList
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

}
