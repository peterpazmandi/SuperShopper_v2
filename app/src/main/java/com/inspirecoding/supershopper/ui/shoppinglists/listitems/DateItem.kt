package com.inspirecoding.supershopper.ui.shoppinglists.listitems

import android.util.Log
import android.view.View
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.databinding.LayoutDateItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.getMonthLongName
import java.util.*

data class DateItem(val shoppingList: ShoppingList): BaseItem<LayoutDateItemBinding> {

    private val TAG = this.javaClass.simpleName

    override val layoutId: Int
        get() = R.layout.layout_date_item

    override val uniqueId = shoppingList.dueDate.time

    override fun initializeViewBinding(view: View) = LayoutDateItemBinding.bind(view)

    override fun bind(
        binding: LayoutDateItemBinding,
        itemClickCallBack: ((BaseItem<LayoutDateItemBinding>) -> Unit)?
    ) {
        binding.tvDate.text = shoppingList.dueDate.getMonthLongName()
    }

}

