package com.inspirecoding.supershopper.utils.baseclasses

import android.view.View
import androidx.viewbinding.ViewBinding

/**
 * List items used in [BaseViewHolder]. Implement this with items containing data to display
 * */
interface BaseItem<T: ViewBinding> {

    val layoutId: Int

    val uniqueId: Any

    fun initializeViewBinding(view: View): T

    /**
     * @param itemClickCallback Optional click callback for clicks on the whole item
     * */
    fun bind(holder: BaseViewHolder<*>, itemClickCallBack: ((View, BaseItem<T>) -> Unit)?) {
        val specificHolder = holder as BaseViewHolder<T>
        bind(specificHolder.binding, itemClickCallBack)
    }

    fun bind(binding: T, itemClickCallBack: ((View, BaseItem<T>) -> Unit)?)

    override fun equals(other: Any?): Boolean

}