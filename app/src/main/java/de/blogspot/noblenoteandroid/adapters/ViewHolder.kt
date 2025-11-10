package de.blogspot.noblenoteandroid.adapters

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import rx.subscriptions.CompositeSubscription

/**
 * Created by Taiko on 26.09.2016.
 */
class ViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    // You might still use this for RxJava subscriptions if you don't use LiveData/Flow with binding
    val mCompositeSubscription = CompositeSubscription()
}