package com.inspirecoding.supershopper.ui.openedshoppinglist.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.OpenedShoppingListDetailsFragmentBinding
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListFragment
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListFragmentDirections
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel
import com.inspirecoding.supershopper.ui.openedshoppinglist.details.membersitem.MembersItem
import com.inspirecoding.supershopper.ui.openedshoppinglist.items.OpenedShoppingListItemsFragmentDirections
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel.Companion.ARG_KEY_DUEDATE
import com.inspirecoding.supershopper.utils.Status
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import com.inspirecoding.supershopper.utils.getDueDateInString
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class OpenedShoppingListDetailsFragment : Fragment(R.layout.opened_shopping_list_details_fragment) {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val viewModel by viewModels<OpenedShoppingListDetailsViewModel>()
    private lateinit var binding : OpenedShoppingListDetailsFragmentBinding
    private lateinit var adapter: BaseListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = OpenedShoppingListDetailsFragmentBinding.bind(view)

        setupShoppingListObserver()
        setupEventHandler()

        binding.ivSetDueDate.setOnClickListener {
            viewModel.onShowDueDatePickerDialog()
        }
        binding.ivAddMembers.setOnClickListener {
            viewModel.onShowFindFriendsDialog()
        }
        binding.tvDeleteLeave.setOnClickListener {
            viewModel.onShowLeaveDeleteDialog()
        }
    }


    private fun setupShoppingListObserver() {
        viewModel.openedShoppingList.observe(viewLifecycleOwner, { result ->

            when(result.status)
            {
                Status.LOADING -> {
                    binding.progressBar.makeItVisible()
                }
                Status.SUCCESS -> {
                    binding.progressBar.makeItInVisible()
                    result.data?.let {
                        updateUi(it)
                    }
                }
                Status.ERROR -> {
                    binding.progressBar.makeItInVisible()
                    result.message?.let {
                        viewModel.onShowErrorMessage(it)
                    }
                }
            }
        })
    }

    private fun updateUi(shoppingList: ShoppingList) {
        binding.apply {

            tvDueDate.text = root.context.getDueDateInString(shoppingList.dueDate)

            val creatorUser = shoppingList.usersSharedWith[0]
            tvCreatedBy.text = creatorUser.name

            if(shoppingList.usersSharedWith[0].profilePicture.isNotEmpty()) {
                Picasso
                    .get()
                    .load(shoppingList.usersSharedWith[0].profilePicture)
                    .fit()
                    .placeholder(R.drawable.ic_default_profile_picture)
                    .into(binding.ivCreatedByProfilePhoto)
            }

            initMembersRecyclerView(shoppingList.usersSharedWith)

            if (creatorUser.id == viewModel.currentUser.value?.id) {
                binding.tvDeleteLeave.text = getString(R.string.delete_shopping_list)
                binding.tvDeleteLeave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_trash, 0, 0, 0)
                binding.tvDeleteLeave.makeItVisible()
            } else {
                binding.tvDeleteLeave.text = getString(R.string.leave_shopping_list)
                binding.tvDeleteLeave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_leave, 0, 0, 0)
                binding.tvDeleteLeave.makeItVisible()
            }
        }
    }

    private fun initMembersRecyclerView(usersSharedWith: MutableList<User>) {

        val listOfMembersItem = mutableListOf<BaseItem<*>>()
        usersSharedWith.forEach {
            listOfMembersItem.add(MembersItem(it))
        }

        adapter = BaseListAdapter { view, selectedItem ->

        }

        binding.rvMembers.setHasFixedSize(true)
        binding.rvMembers.setItemViewCacheSize(20)

        binding.rvMembers.adapter = adapter

        adapter.submitList(listOfMembersItem)
    }

    private fun setupEventHandler() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.listItemEventChannel.collect { event ->
                when(event)
                {
                    is OpenedShoppingListDetailsViewModel.ListDetailsEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                    is OpenedShoppingListDetailsViewModel.ListDetailsEvent.NavigateToFindFriendsDialog -> {
                        navigateToFindFriendsDialog(event.user, event.shoppingList)
                    }
                    is OpenedShoppingListDetailsViewModel.ListDetailsEvent.NavigateToLeaveDeleteDialog -> {
                        navigateToLeaveDeleteDialog(event.user, event.shoppingList)
                    }
                    is OpenedShoppingListDetailsViewModel.ListDetailsEvent.NavigateToDueDatePickerDialog -> {
                        navigateToDueDatePickerDialog(event.dueDate)
                    }
                }
            }
        }
    }



    /** Navigation methods **/
    private fun navigateToFindFriendsDialog(user: User, shoppingList: ShoppingList) {
        val action = OpenedShoppingListFragmentDirections.actionOpenedShoppingListFragmentToFindFriendsBottomSheetFragment(user, shoppingList)
        findNavController().navigate(action)
    }
    private fun navigateToLeaveDeleteDialog(user: User, shoppingList: ShoppingList) {
        val action = OpenedShoppingListFragmentDirections.actionOpenedShoppingListFragmentToLeaveDeleteShoppingListBottomSheetFragment(user, shoppingList)
        findNavController().navigate(action)
    }
    private fun navigateToDueDatePickerDialog(dueDate: Long) {
        val action = OpenedShoppingListFragmentDirections.actionOpenedShoppingListFragmentToSelectDueDateBottomSheetFragment(dueDate)
        findNavController().navigate(action)
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = OpenedShoppingListFragmentDirections.actionOpenedShoppingListFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
}