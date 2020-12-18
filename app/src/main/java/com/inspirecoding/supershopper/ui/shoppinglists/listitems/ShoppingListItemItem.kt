package com.inspirecoding.supershopper.ui.shoppinglists.listitems

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.databinding.LayoutShoppinglistitemItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.getDueDateInString
import com.inspirecoding.supershopper.utils.makeItVisible

data class ShoppingListItemItem(val shoppingList: ShoppingList): BaseItem<LayoutShoppinglistitemItemBinding> {

    override val layoutId = R.layout.layout_shoppinglistitem_item
    override val uniqueId = shoppingList

    override fun initializeViewBinding(view: View) = LayoutShoppinglistitemItemBinding.bind(view)

    override fun bind(
        binding: LayoutShoppinglistitemItemBinding,
        itemClickCallBack: ((BaseItem<LayoutShoppinglistitemItemBinding>) -> Unit)?
    ) {
        val context = binding.root.context

        binding.tvDate.text = context.getDueDateInString(shoppingList.dueDate)
        binding.tvName.text = shoppingList.name

        setProfileImages(binding)

        binding.pbItemProgress.progress = shoppingList.calculateProgress()
        binding.tvTotalAndOpenItemsCount.text = shoppingList.getTotalAndOpenItemsCount()
    }

    private fun setProfileImages(
        binding: LayoutShoppinglistitemItemBinding
    ) {
        val freindsListSize = shoppingList.friendsSharedWith.size
        if(freindsListSize == 1) {
            binding.ivProfilePhoto1.makeItVisible()

            val user = shoppingList.usersSharedWith[0]
            Glide.with(binding.root)
                .load(user.profilePicture)
                .centerCrop()
                .placeholder(R.drawable.ic_default_profile_picture)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivProfilePhoto1)
        }
        if(freindsListSize == 2) {
            binding.ivProfilePhoto2.makeItVisible()

            val user = shoppingList.usersSharedWith[1]
            Glide.with(binding.root)
                .load(user.profilePicture)
                .centerCrop()
                .placeholder(R.drawable.ic_default_profile_picture)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivProfilePhoto2)
        }
        if(freindsListSize == 3) {
            binding.ivProfilePhoto3.makeItVisible()

            val user = shoppingList.usersSharedWith[2]
            Glide.with(binding.root)
                .load(user.profilePicture)
                .centerCrop()
                .placeholder(R.drawable.ic_default_profile_picture)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivProfilePhoto3)
        }
        if(freindsListSize > 3) {
            binding.tvShoppingListSharedWithMore.makeItVisible()
            binding.tvShoppingListSharedWithMore.text = "+${freindsListSize - 3}"
        }
    }

}
