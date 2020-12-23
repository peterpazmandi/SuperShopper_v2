package com.inspirecoding.supershopper.ui.openedshoppinglist.details.membersitem

import android.view.View
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.LayoutFriendItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.squareup.picasso.Picasso

data class MembersItem(val user: User): BaseItem<LayoutFriendItemBinding> {

    // CONST
    private val TAG = this.javaClass.simpleName

    override val layoutId = R.layout.layout_friend_item
    override val uniqueId = user.id
    override val data = user

    override fun initializeViewBinding(view: View) = LayoutFriendItemBinding.bind(view)

    override fun bind(
        binding: LayoutFriendItemBinding,
        itemClickCallBack: ((View, BaseItem<LayoutFriendItemBinding>) -> Unit)?
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
