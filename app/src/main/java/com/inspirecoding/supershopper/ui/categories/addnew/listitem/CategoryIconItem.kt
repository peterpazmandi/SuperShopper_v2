package com.inspirecoding.supershopper.ui.categories.addnew.listitem

import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.LayoutCategoryIconBinding
import com.inspirecoding.supershopper.databinding.LayoutCategoryItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem

data class CategoryIconItem(@DrawableRes val icon: Int): BaseItem<LayoutCategoryIconBinding> {

    override val layoutId: Int = R.layout.layout_category_icon
    override val uniqueId: Any = icon
    override val data: Any = icon

    var isSelected = false

    override fun initializeViewBinding(view: View) = LayoutCategoryIconBinding.bind(view)

    override fun bind(
        binding: LayoutCategoryIconBinding,
        itemClickCallBack: ((View, BaseItem<LayoutCategoryIconBinding>) -> Unit)?
    ) {
        val context = binding.root.context

        binding.ivIcon.setImageDrawable(ContextCompat.getDrawable(context, icon))

        binding.ivIcon.setOnClickListener {
            itemClickCallBack?.invoke(it, this)
        }

        if(isSelected) {
            binding.root.alpha = 1f
        } else {
            binding.root.alpha = 0.4f
        }

    }

}
