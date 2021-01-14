package com.inspirecoding.supershopper.ui.addedititem.listitem

import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.LayoutUnitItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.listOfUnits

data class UnitItem(@StringRes val unit: Int): BaseItem<LayoutUnitItemBinding> {

    override val layoutId = R.layout.layout_unit_item
    override val uniqueId = unit
    override val data = unit

    var isSelected = false

    override fun initializeViewBinding(view: View) = LayoutUnitItemBinding.bind(view)

    override fun bind(
        binding: LayoutUnitItemBinding,
        itemClickCallBack: ((View, BaseItem<LayoutUnitItemBinding>) -> Unit)?
    ) {
        val context = binding.root.context

        binding.tvUnit.text = context.getString(listOfUnits[unit])

        if (isSelected) {
            binding.tvUnit.background = ContextCompat.getDrawable(context, R.drawable.shape_roundedallcorners_blue)
            binding.tvUnit.setTextColor(ContextCompat.getColor(context, R.color.app_background))
        } else {
            binding.tvUnit.background = ContextCompat.getDrawable(context, R.drawable.shape_roundedallcorners_gray)
            binding.tvUnit.setTextColor(ContextCompat.getColor(context, R.color.dark_gray_2))
        }

        binding.tvUnit.setOnClickListener {
            itemClickCallBack?.invoke(it, this)
        }
    }

}
