package com.inspirecoding.supershopper.ui.shoppinglists.listitems

import android.util.Log
import android.view.View
import android.widget.ImageView
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.LayoutShoppinglistitemItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.getDueDateInString
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import com.squareup.picasso.Picasso


data class ShoppingListItemItem(val shoppingList: ShoppingList): BaseItem<LayoutShoppinglistitemItemBinding> {

    // CONST
    private val TAG = this.javaClass.simpleName

    override val layoutId = R.layout.layout_shoppinglistitem_item
    override val uniqueId = shoppingList.dueDate.time

    override fun initializeViewBinding(view: View) = LayoutShoppinglistitemItemBinding.bind(view)

    override fun bind(
        binding: LayoutShoppinglistitemItemBinding,
        itemClickCallBack: ((View, BaseItem<LayoutShoppinglistitemItemBinding>) -> Unit)?
    ) {
        val context = binding.root.context

        binding.root.setOnClickListener {
            itemClickCallBack?.invoke(it, this)
        }

        binding.tvDate.text = context.getDueDateInString(shoppingList.dueDate)

        binding.tvName.text = shoppingList.name

        setProfileImages(binding)

        binding.pbItemProgress.progress = shoppingList.calculateProgress()
        binding.tvTotalAndOpenItemsCount.text = shoppingList.getTotalAndOpenItemsCount()

        if(shoppingList.calculateProgress() == 100) {
            binding.ivDoneMark.makeItVisible()
            binding.tvTotalAndOpenItemsCount.makeItInVisible()
        } else {
            binding.ivDoneMark.makeItInVisible()
            binding.tvTotalAndOpenItemsCount.makeItVisible()
        }
    }

    private fun setProfileImages(
        binding: LayoutShoppinglistitemItemBinding
    ) {
        val friendsListSize = shoppingList.usersSharedWith.size

        for (i in 0 until friendsListSize) {
            if(i == 0) {
                binding.ivProfilePhoto1.makeItVisible()

                val user = shoppingList.usersSharedWith[0]
                setProfilePictures(user, binding.ivProfilePhoto1)
            }
            if(i == 1) {
                binding.ivProfilePhoto2.makeItVisible()

                val user = shoppingList.usersSharedWith[1]
                setProfilePictures(user, binding.ivProfilePhoto2)
            }
            if(i == 2) {
                binding.ivProfilePhoto3.makeItVisible()

                val user = shoppingList.usersSharedWith[2]
                setProfilePictures(user, binding.ivProfilePhoto3)
            }
            if(i > 2) {
                binding.tvShoppingListSharedWithMore.makeItVisible()
                binding.tvShoppingListSharedWithMore.text = "+${friendsListSize - 3}"
                break
            }
        }
    }
    private fun setProfilePictures(user: User?, imageView: ImageView) {
        user?.let {
            if(user.profilePicture.isNotEmpty()) {
                Picasso
                    .get()
                    .load(user.profilePicture)
                    .fit()
                    .placeholder(R.drawable.ic_default_profile_picture)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_default_profile_picture)
            }
        }
    }

}
