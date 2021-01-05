package com.inspirecoding.supershopper.ui.friends.searchfriends

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.SearchFriendsFragmentBinding
import com.inspirecoding.supershopper.ui.friends.FriendsFragmentDirections
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SearchFriendsFragment : Fragment(R.layout.search_friends_fragment) {

    private val viewModel: SearchFriendsViewModel by viewModels()
    private lateinit var binding: SearchFriendsFragmentBinding
    private lateinit var adapter: BaseListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SearchFriendsFragmentBinding.bind(view)

        initRecyclerView()
        setupEvents()

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tietSearchField.doAfterTextChanged {
            viewModel.searchFriend(it.toString())
        }
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.searchFriendsEvents.collect { event ->
                when(event)
                {
                    SearchFriendsViewModel.SearchFriendsFragmentsEvent.LessThenFiveCharacters -> {
                        binding.llSearchFriendsScreen.makeItVisible()

                        binding.progressBar.makeItInVisible()
                        binding.llNoResult.makeItInVisible()
                        binding.rvUsers.makeItInVisible()
                    }
                    SearchFriendsViewModel.SearchFriendsFragmentsEvent.NoUserFound -> {
                        binding.llNoResult.makeItVisible()

                        binding.progressBar.makeItInVisible()
                        binding.llSearchFriendsScreen.makeItInVisible()
                        binding.rvUsers.makeItInVisible()
                    }
                    SearchFriendsViewModel.SearchFriendsFragmentsEvent.ShowLoading -> {
                        binding.progressBar.makeItVisible()

                        binding.llSearchFriendsScreen.makeItInVisible()
                        binding.llNoResult.makeItInVisible()
                        binding.rvUsers.makeItInVisible()
                    }
                    is SearchFriendsViewModel.SearchFriendsFragmentsEvent.ShowResult -> {
                        adapter.submitList(event.listOfFriends)
                        binding.rvUsers.makeItVisible()

                        binding.progressBar.makeItInVisible()

                        binding.llSearchFriendsScreen.makeItInVisible()
                        binding.llNoResult.makeItInVisible()
                    }
                    is SearchFriendsViewModel.SearchFriendsFragmentsEvent.NavigateToProfileFragment -> {
                        navigateToUserProfileFragment(event.user, event.selectedUser)
                    }
                    is SearchFriendsViewModel.SearchFriendsFragmentsEvent.ShowErrorMessage -> {
                        binding.progressBar.makeItInVisible()
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        adapter = BaseListAdapter { view, selectedUser ->
            viewModel.onNavigateToProfileFragmentSelected(selectedUser.data as User)
        }

        binding.rvUsers.adapter = adapter
    }











    /** Navigation methods **/
    private fun navigateToUserProfileFragment(user: User, selectedUser: User) {
        val action = SearchFriendsFragmentDirections.actionSearchFriendsFragmentToUserProfileFragment(user, selectedUser)
        findNavController().navigate(action)
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = SearchFriendsFragmentDirections.actionSearchFriendsFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
}