package com.inspirecoding.supershopper.ui.openedshoppinglist.items.listitem

import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ListItem
import com.inspirecoding.supershopper.databinding.LayoutListitemItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem

data class ListItemsItem(val listItem: ListItem): BaseItem<LayoutListitemItemBinding> {

    override val layoutId = R.layout.layout_listitem_item
    override val uniqueId = listItem.id
    override val data = listItem

    override fun initializeViewBinding(view: View) = LayoutListitemItemBinding.bind(view)

    override fun bind(
        binding: LayoutListitemItemBinding,
        itemClickCallBack: ((View, BaseItem<LayoutListitemItemBinding>) -> Unit)?
    ) {

        changeItemUiIfCheckChange(binding, listItem.isBought)

        binding.tvName.text = listItem.item
        binding.tvUnit.text = "${listItem.qunatity} ${listItem.unit}"

        binding.chbDone.isChecked = listItem.isBought
        binding.chbDone.setOnClickListener {
            itemClickCallBack?.invoke(it, this)

            changeItemUiIfCheckChange(binding, (it as AppCompatCheckBox).isChecked)
        }
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

            binding.root.background = ContextCompat.getDrawable(binding.root.context, R.color.white)

        }
    }
}
