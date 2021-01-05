package com.inspirecoding.supershopper.ui.friends

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.FriendsFragmentBinding
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsFragmentDirections
import com.inspirecoding.supershopper.utils.Status
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
        viewModel.getFriendsAlphabeticalList()
        setupEvents()

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivSearchFriend.setOnClickListener {
            viewModel.onSearchFriendSelected()
        }
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.friendsEvents.collect { event ->
                when(event)
                {
                    is FriendsViewModel.FriendsFragmentsEvent.NavigateSearchFriendsFragment -> {
                        navigateToSearchFrindsFragment(event.user)
                    }
                    is FriendsViewModel.FriendsFragmentsEvent.NavigateFriendRequestsFragment -> {

                    }
                    is FriendsViewModel.FriendsFragmentsEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        adapter = BaseListAdapter { view, selectedItem ->

        }
        binding.rvFriends.setHasFixedSize(true)
        binding.rvFriends.setItemViewCacheSize(50)

        binding.rvFriends.adapter = adapter
    }

    private fun setupFriendsListObserver() {
        viewModel.listOfFriends.observe(viewLifecycleOwner, { result ->
            when (result.status) {
                LOADING -> {
                    binding.progressBar.makeItVisible()
                }
                SUCCESS -> {
                    binding.progressBar.makeItInVisible()
                    result.data?.let { listOfFriends ->
                        if(listOfFriends.isEmpty()) {
                            binding.clNoFriends.makeItVisible()
                        }

                        val listOfUserObjects = viewModel.createListOfFriendsItem(listOfFriends)
                        adapter.submitList(listOfUserObjects)
                    }
                }
                ERROR -> {
                    binding.progressBar.makeItInVisible()
                }
            }
        })
    }











    /** Navigation methods **/
    private fun navigateToSearchFrindsFragment(user: User) {
        val action = FriendsFragmentDirections.actionFriendsFragmentToSearchFriendsFragment(user)
        findNavController().navigate(action)
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = FriendsFragmentDirections.actionFriendsFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
}