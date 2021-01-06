package com.inspirecoding.supershopper.ui.userprofile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.UserProfileFragmentBinding
import com.inspirecoding.supershopper.ui.friends.searchfriends.SearchFriendsFragmentDirections
import com.inspirecoding.supershopper.ui.friends.searchfriends.SearchFriendsViewModel
import com.inspirecoding.supershopper.utils.enums.FriendshipStatus
import com.inspirecoding.supershopper.utils.enums.FriendshipStatus.*
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class UserProfileFragment : Fragment(R.layout.user_profile_fragment) {

    private val viewModel: UserProfileViewModel by viewModels()
    private lateinit var binding: UserProfileFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = UserProfileFragmentBinding.bind(view)

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        setupFriendshipStatusObserver()
        setupEvents()

        binding.tvSendFriendRequest.setOnClickListener {
            viewModel.onSendFriendRequest()
        }
        binding.tvAcceptFriendRequest.setOnClickListener {
            viewModel.onAcceptFriendRequest()
        }
        binding.tvDeclineFriendRequest.setOnClickListener {
            viewModel.onRemoveFriendRequest()
        }
        binding.tvRemoveFriendRequest.setOnClickListener {
            viewModel.onRemoveFriendRequest()
        }
        binding.tvUnfriend.setOnClickListener {
            viewModel.onUnfriend()
        }
    }

    private fun setupFriendshipStatusObserver() {
        viewModel.selectedUser.observe(viewLifecycleOwner, { selectedUser ->
            updateProfileUi(selectedUser)
        })
    }

    private fun updateProfileUi(selectedUser: User) {

        binding.tvUsersName.text = selectedUser.name

        if(selectedUser.profilePicture.isNotEmpty()) {
            Picasso
                .get()
                .load(selectedUser.profilePicture)
                .placeholder(R.drawable.ic_default_profile_picture)
                .centerCrop()
                .fit()
                .into(binding.ivProfilePhoto)
        }

    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fragmentEvent.collect { event ->
                when(event)
                {
                    UserProfileViewModel.FragmentEvent.ShowLoading -> {
                        binding.progressBar.makeItVisible()

                        binding.tvSendFriendRequest.makeItInVisible()
                        binding.tvUnfriend.makeItInVisible()
                        binding.tvRemoveFriendRequest.makeItInVisible()
                        binding.tvAcceptFriendRequest.makeItInVisible()
                        binding.tvDeclineFriendRequest.makeItInVisible()
                    }
                    is UserProfileViewModel.FragmentEvent.ShowResult -> {
                        binding.progressBar.makeItInVisible()

                        when(event.friendShopStatus)
                        {
                            NOFRIENDSHIP -> {
                                binding.tvSendFriendRequest.makeItVisible()
                            }
                            FRIENDS -> {
                                binding.tvUnfriend.makeItVisible()
                            }
                            SENDER -> {
                                binding.tvRemoveFriendRequest.makeItVisible()
                            }
                            RECEIVER -> {
                                binding.tvAcceptFriendRequest.makeItVisible()
                                binding.tvDeclineFriendRequest.makeItVisible()
                            }
                        }
                    }
                    is UserProfileViewModel.FragmentEvent.ShowErrorMessage -> {
                        binding.progressBar.makeItInVisible()
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }










    /** Navigation methods **/
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = UserProfileFragmentDirections.actionFriendsFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }

}