package com.inspirecoding.supershopper.ui.shoppinglists.listitems

import android.view.View
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.LayoutDateItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.getMonthLongName
import java.util.*

data class DateItem(val date: Date): BaseItem<LayoutDateItemBinding> {

    override val layoutId: Int
        get() = R.layout.layout_date_item
    override val uniqueId: Any
        get() = date.time

    override fun initializeViewBinding(view: View) = LayoutDateItemBinding.bind(view)

    override fun bind(
        binding: LayoutDateItemBinding,
        itemClickCallBack: ((BaseItem<LayoutDateItemBinding>) -> Unit)?
    ) {
        binding.tvDate.text = date.getMonthLongName()
    }

}

