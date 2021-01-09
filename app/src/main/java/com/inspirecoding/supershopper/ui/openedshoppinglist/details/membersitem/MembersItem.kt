package com.inspirecoding.supershopper.ui.openedshoppinglist.details.membersitem

import android.view.View
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.LayoutMemberItemBinding
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.squareup.picasso.Picasso

data class MembersItem(val user: User): BaseItem<LayoutMemberItemBinding> {

    // CONST
    private val TAG = this.javaClass.simpleName

    override val layoutId = R.layout.layout_member_item
    override val uniqueId = user.id
    override val data = user

    override fun initializeViewBinding(view: View) = LayoutMemberItemBinding.bind(view)

    override fun bind(
        binding: LayoutMemberItemBinding,
        itemClickCallBack: ((View, BaseItem<LayoutMemberItemBinding>) -> Unit)?
    ) {

        binding.tvName.text = user.name

        if(user.profilePicture.isNotEmpty()) {
            Picasso
                .get()
                .load(user.profilePicture)
                .fit()
                .placeholder(R.drawable.ic_default_profile_picture)
                .into(binding.ivProfilePhoto)
        }

    }
}
