package com.inspirecoding.supershopper.ui.categories.listitems

import android.view.View
import androidx.core.content.ContextCompat
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.LayoutCategoryItemBinding
import com.inspirecoding.supershopper.databinding.LayoutUserItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import com.squareup.picasso.Picasso

data class UserItem(val user: User): BaseItem<LayoutUserItemBinding> {

    override val layoutId = R.layout.layout_user_item
    override val uniqueId = user.id
    override val data = user

    var isSelected = false

    override fun initializeViewBinding(view: View) = LayoutUserItemBinding.bind(view)

    override fun bind(
        binding: LayoutUserItemBinding,
        itemClickCallBack: ((View, BaseItem<LayoutUserItemBinding>) -> Unit)?
    ) {
        val context = binding.root.context

        binding.root.setOnClickListener {
            isSelected = !isSelected
            if(isSelected) {
                binding.ivSelection.makeItVisible()
            } else {
                binding.ivSelection.makeItInVisible()
            }

            itemClickCallBack?.invoke(it, this)
        }

        binding.tvUsersName.text = user.name

        if(user.profilePicture.isNotEmpty()) {
            Picasso
                .get()
                .load(user.profilePicture)
                .fit()
                .placeholder(R.drawable.ic_default_profile_picture)
                .into(binding.ivProfilePhoto)
        }

        if(isSelected) {
            binding.ivSelection.makeItVisible()
        } else {
            binding.ivSelection.makeItInVisible()
        }
    }

}