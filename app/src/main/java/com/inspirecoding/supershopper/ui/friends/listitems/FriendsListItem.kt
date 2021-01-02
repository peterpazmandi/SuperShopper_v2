package com.inspirecoding.supershopper.ui.friends.listitems

import android.view.View
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.LayoutFriendslistItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.squareup.picasso.Picasso

data class FriendsListItem(val user: User): BaseItem<LayoutFriendslistItemBinding> {

    override val layoutId = R.layout.layout_friendslist_item
    override val uniqueId = user.id
    override val data = user

    override fun initializeViewBinding(view: View) = LayoutFriendslistItemBinding.bind(view)

    override fun bind(
        binding: LayoutFriendslistItemBinding,
        itemClickCallBack: ((View, BaseItem<LayoutFriendslistItemBinding>) -> Unit)?
    ) {

        binding.tvName.text = user.name

        Picasso
            .get()
            .load(user.profilePicture)
            .fit()
            .placeholder(R.drawable.ic_default_profile_picture)
            .into(binding.ivProfilePhoto)

    }
}
