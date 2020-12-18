package com.inspirecoding.supershopper.utils.baseclasses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import java.lang.IllegalStateException


/**
 * A base adapter that most RecyclerViews should be able to use
 * The style of this adapter, with [BaseItem] used for the list, is based on the excellent
 * [Groupie](https://github.com/lisawray/groupie) library.
 *
 * @param itemClickCallback An optional callback for clicks on an item
 * */
class BaseListAdapter (
    private val itemClickCallback: ((BaseItem<*>) -> Unit)?
) : ListAdapter<BaseItem<*>, BaseViewHolder<*>>(

    AsyncDifferConfig.Builder(object : DiffUtil.ItemCallback<BaseItem<*>>() {
        override fun areItemsTheSame(oldItem: BaseItem<*>, newItem: BaseItem<*>): Boolean {
            return oldItem.uniqueId == newItem.uniqueId
        }

        override fun areContentsTheSame(oldItem: BaseItem<*>, newItem: BaseItem<*>): Boolean {
            return oldItem == newItem
        }
    }).build()

) {

    private var lastItemForViewTypeLookup: BaseItem<*>? = null

    override fun getItemViewType(position: Int) = getItem(position).layoutId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {

        val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        val item = getItemForViewType(viewType)
        return BaseViewHolder(item.initializeViewBinding(itemView))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        getItem(position).bind(holder, itemClickCallback)
    }



    private fun getItemForViewType(viewType: Int): BaseItem<*> {
        val lastItemForViewTypeLookup = lastItemForViewTypeLookup
        if (lastItemForViewTypeLookup != null
            && lastItemForViewTypeLookup.layoutId == viewType
        ) {
            // We expect this to be a hit 100% of the time
            return lastItemForViewTypeLookup
        }

        // To be extra safe in case RecyclerView implementation details change...
        for (i in 0 until itemCount) {
            val item: BaseItem<*> = getItem(i)
            if (item.layoutId == viewType) {
                return item
            }
        }
        throw IllegalStateException("Could not find model for view type: $viewType")
    }











}