package com.inspirecoding.supershopper.ui.friends.searchfriends.listitem

import android.view.View
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.LayoutSearchfriendsItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.squareup.picasso.Picasso

data class SearchFriendItem(val user: User): BaseItem<LayoutSearchfriendsItemBinding> {

    override val layoutId = R.layout.layout_searchfriends_item
    override val uniqueId = user.id
    override val data = user

    override fun initializeViewBinding(view: View) = LayoutSearchfriendsItemBinding.bind(view)
    override fun bind(
        binding: LayoutSearchfriendsItemBinding,
        itemClickCallBack: ((View, BaseItem<LayoutSearchfriendsItemBinding>) -> Unit)?
    ) {
        val context = binding.root.context

        binding.btnViewProfile.setOnClickListener {

            itemClickCallBack?.invoke(it, this)
        }

        binding.tvName.text = user.name

        if(user.profilePicture.isNotEmpty()) {
            Picasso
                .get()
                .load(user.profilePicture)
                .placeholder(R.drawable.ic_default_profile_picture)
                .centerCrop()
                .fit()
                .into(binding.ivProfilePhoto)
        }

    }

}
