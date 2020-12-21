package com.inspirecoding.supershopper.utils.baseclasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import java.lang.IllegalStateException
import java.util.*


/**
 * A base adapter that most RecyclerViews should be able to use
 * The style of this adapter, with [BaseItem] used for the list, is based on the excellent
 * [Groupie](https://github.com/lisawray/groupie) library.
 *
 * @param itemClickCallback An optional callback for clicks on an item
 * */
class BaseListAdapter (
    private val itemClickCallback: ((View, BaseItem<*>) -> Unit)?
) : ListAdapter<BaseItem<*>, BaseViewHolder<*>>(DiffCallback()), ContentTouchHelperAdapter {

    private var lastItemForViewTypeLookup: BaseItem<*>? = null

    private var dataList = ArrayList<BaseItem<*>>()
    fun setDataList(data: List<BaseItem<*>>) {
        dataList.clear()
        dataList.addAll(data)
    }

    override fun getItemViewType(position: Int) = getItem(position).layoutId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {

        val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        val item = getItemForViewType(viewType)
        return BaseViewHolder(item.initializeViewBinding(itemView))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        getItem(position).bind(holder, itemClickCallback)
    }

    override fun onItemDeleted(fromPosition: Int): Boolean {
        dataList.removeAt(fromPosition)
        notifyItemRemoved(fromPosition)
        submitList(dataList)
        return true
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(dataList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        submitList(dataList)
        return true
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

    class DiffCallback : DiffUtil.ItemCallback<BaseItem<*>>() {
        override fun areItemsTheSame(oldItem: BaseItem<*>, newItem: BaseItem<*>): Boolean {
            return oldItem.uniqueId == newItem.uniqueId
        }

        override fun areContentsTheSame(oldItem: BaseItem<*>, newItem: BaseItem<*>): Boolean {
            return oldItem == newItem
        }
    }


}

interface ContentTouchHelperAdapter {
    fun onItemDeleted(fromPosition: Int): Boolean
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
}