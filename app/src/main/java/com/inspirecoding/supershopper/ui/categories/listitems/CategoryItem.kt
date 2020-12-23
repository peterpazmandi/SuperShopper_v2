package com.inspirecoding.supershopper.ui.categories.listitems

import android.view.View
import androidx.core.content.ContextCompat
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.databinding.LayoutCategoryItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem

data class CategoryItem(val category: Category): BaseItem<LayoutCategoryItemBinding> {

    override val layoutId = R.layout.layout_category_item
    override val uniqueId = category.position
    override val data = category

    override fun initializeViewBinding(view: View) = LayoutCategoryItemBinding.bind(view)

    override fun bind(
        binding: LayoutCategoryItemBinding,
        itemClickCallBack: ((View, BaseItem<LayoutCategoryItemBinding>) -> Unit)?
    ) {
        val context = binding.root.context

        if (category.nameStringResId != null) {
            binding.tvCategory.text = context.getString(category.nameStringResId)
        } else {
            binding.tvCategory.text = category.customName
        }

        binding.ivIcon.setImageDrawable(ContextCompat.getDrawable(context, category.iconDrawableResId))

        binding.ivMoveUp.setOnClickListener {
            itemClickCallBack?.invoke(it, this)
        }
        binding.ivMoveDown.setOnClickListener {
            itemClickCallBack?.invoke(it, this)
        }
        binding.ivDelete.setOnClickListener {
            itemClickCallBack?.invoke(it, this)
        }

    }

}