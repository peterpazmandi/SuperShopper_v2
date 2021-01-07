package com.inspirecoding.supershopper.ui.shoppinglists.listitems

import android.view.View
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.databinding.LayoutDateItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.getMonthLongName

data class DateItem(val shoppingList: ShoppingList): BaseItem<LayoutDateItemBinding> {

    private val TAG = this.javaClass.simpleName

    override val layoutId = R.layout.layout_date_item
    override val uniqueId = shoppingList.shoppingListId
    override val data = shoppingList

    override fun initializeViewBinding(view: View) = LayoutDateItemBinding.bind(view)

    override fun bind(
        binding: LayoutDateItemBinding,
        itemClickCallBack: ((View, BaseItem<LayoutDateItemBinding>) -> Unit)?
    ) {
        binding.tvDate.text = shoppingList.dueDate.getMonthLongName()
    }

}

