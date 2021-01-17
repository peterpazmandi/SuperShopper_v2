package com.inspirecoding.supershopper.ui.friends

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.FriendsFragmentBinding
import com.inspirecoding.supershopper.utils.Status.*
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class FriendsFragment : Fragment(R.layout.friends_fragment) {

    private val viewModel: FriendsViewModel by viewModels()
    private lateinit var binding: FriendsFragmentBinding
    private lateinit var adapter: BaseListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FriendsFragmentBinding.bind(view)

        initRecyclerView()
        setupFriendsListObserver()
        setupNumberOfPendingFriendRequestsObserver()
        setupEvents()
        viewModel.getFriendsAlphabeticalList()
        viewModel.getListOfPendingFriendRequests()

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivSearchFriend.setOnClickListener {
            viewModel.onSearchFriendSelected()
        }

        binding.btnFindYourFriends.setOnClickListener {
            viewModel.onSearchFriendSelected()
        }

        binding.btnInviteFriends.setOnClickListener {
            viewModel.onShareTheAppSelected()
        }

        binding.ivFriendRequests.setOnClickListener {
            viewModel.onFriendRequestsSelected()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.onRefreshList()
        }
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.friendsEvents.collect { event ->
                when(event)
                {
                    is FriendsViewModel.FriendsFragmentsEvent.NavigateToSearchFriendsFragment -> {
                        navigateToSearchFriendsFragment(event.user)
                    }
                    is FriendsViewModel.FriendsFragmentsEvent.NavigateToUserProfileFragment -> {
                        navigateToUserProfileFragment(event.user, event.selectedUser)
                    }
                    is FriendsViewModel.FriendsFragmentsEvent.NavigateToFriendRequestsFragment -> {
                        navigateToFriendRequestsFragment(event.user)
                    }
                    is FriendsViewModel.FriendsFragmentsEvent.ShareTheApp -> {
                        shareTheApp(event.user)
                    }
                    is FriendsViewModel.FriendsFragmentsEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        adapter = BaseListAdapter { _, selectedItem ->
            viewModel.onUserProfileSelected(selectedUser = selectedItem.data as User)
        }
        binding.rvFriends.setHasFixedSize(true)
        binding.rvFriends.setItemViewCacheSize(50)

        binding.rvFriends.adapter = adapter

        binding.rvFriends.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(!recyclerView.canScrollVertically(1)) {
                    viewModel.getFriendsAlphabeticalList()
                }
            }
        })
    }

    private fun setupFriendsListObserver() {
        viewModel.listOfFriends.observe(viewLifecycleOwner, { result ->
            when (result.status) {
                LOADING -> {
                    binding.progressBar.makeItVisible()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
                SUCCESS -> {
                    binding.progressBar.makeItInVisible()
                    binding.swipeRefreshLayout.isRefreshing = false
                    result.data?.let { listOfFriends ->
                        if(listOfFriends.isEmpty()) {
                            binding.clNoFriends.makeItVisible()
                        } else {
                            binding.clNoFriends.makeItInVisible()
                            binding.swipeRefreshLayout.isRefreshing = false
                        }

                        val listOfUserObjects = viewModel.createListOfFriendsItem(listOfFriends)
                        adapter.submitList(listOfUserObjects)
                    }
                }
                ERROR -> {
                    binding.progressBar.makeItInVisible()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    private fun setupNumberOfPendingFriendRequestsObserver() {
        viewModel.numberOfPendingFriendRequests.observe(viewLifecycleOwner, { number ->
            if(number > 0) {
                binding.tvPendingRequestsCount.text = number.toString()
                binding.ivFriendRequests.makeItVisible()
                binding.tvPendingRequestsCount.makeItVisible()
            } else {
                binding.ivFriendRequests.makeItInVisible()
                binding.tvPendingRequestsCount.makeItInVisible()
            }
        })
    }

    private fun shareTheApp(currentUser: User) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_the_app))
        intent.putExtra(Intent.EXTRA_TEXT, "${getString(R.string.user_would_like_to_invite_you_to_the_supershopper_app_you_can_download_it_via_the_link, currentUser.name)}\nhttps://play.google.com/store/apps/details?id=com.inspirecoding.supershopper")
        startActivity(Intent.createChooser(intent, getString(R.string.share_the_app_via)))
    }











    /** Navigation methods **/
    private fun navigateToSearchFriendsFragment(user: User) {
        val action = FriendsFragmentDirections.actionFriendsFragmentToSearchFriendsFragment(user)
        findNavController().navigate(action)
    }
    private fun navigateToFriendRequestsFragment(user: User) {
        val action = FriendsFragmentDirections.actionFriendsFragmentToPendingFriendRequestsFragment(user)
        findNavController().navigate(action)
    }
    private fun navigateToUserProfileFragment(user: User, selectedUser: User) {
        val action = FriendsFragmentDirections.actionFriendsFragmentToUserProfileFragment(user, selectedUser)
        findNavController().navigate(action)
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = FriendsFragmentDirections.actionFriendsFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
}