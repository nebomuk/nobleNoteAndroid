package com.taiko.noblenote.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by Taiko on 26.09.2016.
 */

public class ViewHolder extends RecyclerView.ViewHolder
{
    public CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public ViewHolder(View itemView)
    {
        super(itemView);
    }
}
