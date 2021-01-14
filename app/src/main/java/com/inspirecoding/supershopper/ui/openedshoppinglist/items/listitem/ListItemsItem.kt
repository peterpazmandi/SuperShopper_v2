package com.inspirecoding.supershopper.ui.openedshoppinglist.items.listitem

import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.data.ListItem
import com.inspirecoding.supershopper.databinding.LayoutListitemItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.listOfCategoryIcons
import com.inspirecoding.supershopper.utils.listOfUnits

data class ListItemsItem(val listItem: ListItem): BaseItem<LayoutListitemItemBinding> {

    override val layoutId = R.layout.layout_listitem_item
    override val uniqueId = listItem.id
    override val data = listItem

    var category: Category? = null

    override fun initializeViewBinding(view: View) = LayoutListitemItemBinding.bind(view)

    override fun bind(
        binding: LayoutListitemItemBinding,
        itemClickCallBack: ((View, BaseItem<LayoutListitemItemBinding>) -> Unit)?
    ) {
        val context = binding.root.context

        changeItemUiIfCheckChange(binding, listItem.isBought)

        binding.tvName.text = listItem.item

        val unit = listItem.unit.toIntOrNull()?.let {
            context.getString(listOfUnits[listItem.unit.toInt()])
        } ?: listItem.unit
        binding.tvUnit.text = "${listItem.qunatity} $unit"

        binding.chbDone.isChecked = listItem.isBought
        binding.chbDone.setOnClickListener {
            itemClickCallBack?.invoke(it, this)

            changeItemUiIfCheckChange(binding, (it as AppCompatCheckBox).isChecked)
        }
        binding.root.setOnClickListener {
            itemClickCallBack?.invoke(it, this)
        }

        category?.let { _category ->
            binding.ivCategory.setImageDrawable(ContextCompat.getDrawable(context, listOfCategoryIcons[_category.iconDrawableResId]))
        }

        binding.tvComment.text = listItem.comment
    }

    private fun changeItemUiIfCheckChange(binding: LayoutListitemItemBinding, checked: Boolean) {

        if (checked) {

            binding.tvName.alpha = 0.4f
            binding.tvComment.alpha = 0.4f
            binding.tvUnit.alpha = 0.4f
            binding.ivCategory.alpha = 0.4f
            binding.chbDone.alpha = 0.4f

            binding.root.background = ContextCompat.getDrawable(binding.root.context, R.color.gray)

        } else {

            binding.tvName.alpha = 1f
            binding.tvComment.alpha = 1f
            binding.tvUnit.alpha = 1f
            binding.ivCategory.alpha = 1f
            binding.chbDone.alpha = 1f

            binding.root.background = ContextCompat.getDrawable(binding.root.context, R.color.app_background)

        }
    }
}
