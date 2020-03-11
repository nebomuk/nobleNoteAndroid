package com.taiko.noblenote;

import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 *
 * based on
 *
 * https://github.com/radzio/android-data-binding-recyclerview/blob/master/recyclerview-binding/src/main/java/net/droidlabs/mvvm/recyclerview/adapter/BindingRecyclerViewAdapter.java
 */

public class WeakReferenceOnListChangedCallback<T> extends ObservableList.OnListChangedCallback<ObservableList<T>>
{

    private final WeakReference<RecyclerFileAdapter> adapterReference;

    public WeakReferenceOnListChangedCallback(RecyclerFileAdapter bindingRecyclerViewAdapter)
    {
        this.adapterReference = new WeakReference<>(bindingRecyclerViewAdapter);
    }

    @Override
    public void onChanged(ObservableList<T> sender)
    {
        RecyclerView.Adapter adapter = adapterReference.get();
        if (adapter != null)
        {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemRangeChanged(ObservableList<T> sender, int positionStart, int itemCount)
    {
        RecyclerView.Adapter adapter = adapterReference.get();
        if (adapter != null)
        {
            adapter.notifyItemRangeChanged(positionStart, itemCount);
        }
    }

    @Override
    public void onItemRangeInserted(ObservableList<T> sender, int positionStart, int itemCount)
    {
        RecyclerView.Adapter adapter = adapterReference.get();
        if (adapter != null)
        {
            adapter.notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    @Override
    public void onItemRangeMoved(ObservableList<T> sender, int fromPosition, int toPosition, int itemCount)
    {
        RecyclerView.Adapter adapter = adapterReference.get();
        if (adapter != null)
        {
            adapter.notifyItemMoved(fromPosition, toPosition);
        }
    }

    @Override
    public void onItemRangeRemoved(ObservableList<T> sender, int positionStart, int itemCount)
    {
        RecyclerView.Adapter adapter = adapterReference.get();
        if (adapter != null)
        {
            adapter.notifyItemRangeRemoved(positionStart, itemCount);
        }
    }
}
