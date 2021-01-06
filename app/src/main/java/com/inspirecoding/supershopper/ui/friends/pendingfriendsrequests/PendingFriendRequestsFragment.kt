package com.inspirecoding.supershopper.ui.friends.pendingfriendsrequests

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.PendingFriendRequestsFragmentBinding
import com.inspirecoding.supershopper.ui.friends.searchfriends.SearchFriendsFragmentDirections
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class PendingFriendRequestsFragment : Fragment(R.layout.pending_friend_requests_fragment) {

    private val viewModel: PendingFriendRequestsViewModel by viewModels()
    private lateinit var binding: PendingFriendRequestsFragmentBinding
    private lateinit var adapter: BaseListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PendingFriendRequestsFragmentBinding.bind(view)

        setupEvents()
        setupListOfPendingFriendRequestsObserver()
        initRecyclerView()

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fragmentEvents.collect { event ->
                when(event)
                {
                    is PendingFriendRequestsViewModel.FragmentEvent.NavigateToUserProfileFragment -> {
                        navigateToUserProfileFragment(event.user, event.selectedUser)
                    }
                    is PendingFriendRequestsViewModel.FragmentEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }

    private fun setupListOfPendingFriendRequestsObserver() {
        viewModel.listOfPendingFriendRequests.observe(viewLifecycleOwner, { list ->
            val itemsList = viewModel.createSearchFriendsItemsListComparedToFriends(list)
            adapter.submitList(itemsList)
        })
    }

    private fun initRecyclerView() {
        adapter = BaseListAdapter { _, selectedUser ->
            viewModel.onNavigateToProfileFragmentSelected(selectedUser.data as User)
        }

        binding.rvUsers.adapter = adapter
    }











    /** Navigation methods **/
    private fun navigateToUserProfileFragment(user: User, selectedUser: User) {
        val action = PendingFriendRequestsFragmentDirections.actionPendingFriendRequestsFragmentToUserProfileFragment(user, selectedUser)
        findNavController().navigate(action)
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = PendingFriendRequestsFragmentDirections.actionPendingFriendRequestsFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
}