package com.inspirecoding.supershopper.ui.openedshoppinglist.details.leavedeleteshoppinglist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.LeaveDeleteShoppingListBottomSheetFragmentBinding
import com.inspirecoding.supershopper.ui.findfriends.FindFriendsBottomSheetFragmentDirections
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class LeaveDeleteShoppingListBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: LeaveDeleteShoppingListBottomSheetViewModel by viewModels()
    private lateinit var binding: LeaveDeleteShoppingListBottomSheetFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LeaveDeleteShoppingListBottomSheetFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEventHandler()
        setupShoppingListAndCurrentUserObserver()

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnYesSure.setOnClickListener {
            viewModel.onNavigateBackWithResult()
        }
    }

    private fun setupEventHandler() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.leaveDeleteEventChannel.collect { event ->
                when(event)
                {
                    is LeaveDeleteShoppingListBottomSheetViewModel.LeaveDeleteShoppingListEvent.NavigateBackWithResult -> {
                        event.listOfMembers?.let { _listOfMembers ->
                            event.currentUserId?.let { _currentUserId ->
                                navigateBackWithResult(_listOfMembers, _currentUserId)
                            }
                        }
                    }
                    is LeaveDeleteShoppingListBottomSheetViewModel.LeaveDeleteShoppingListEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }

    private fun setupShoppingListAndCurrentUserObserver() {
        viewModel.shoppingListWithCurrentUser.observe(viewLifecycleOwner, {
            updateUi(it)
        })
    }

    private fun updateUi(pair: Pair<ShoppingList?, User?>?) {
        val listOfMembers = pair?.first?.friendsSharedWith
        val currentUserId = pair?.second?.id

        if(listOfMembers != null && currentUserId != null) {
            if(listOfMembers[0] == currentUserId) {
                binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_trash, 0, 0, 0)
                binding.tvTitle.text = getString(R.string.delete_shopping_list)
                binding.tvAreYouSureText.text = getString(R.string.are_you_sure_you_want_to_delete_your_shopping_list)
            } else {
                binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_leave, 0, 0, 0)
                binding.tvTitle.text = getString(R.string.leave_shopping_list)
                binding.tvAreYouSureText.text = getString(R.string.are_you_sure_you_want_to_leave_your_shopping_list)
            }
        }
    }



    /** Navigation methods **/
    private fun navigateBackWithResult(listOfMembers: MutableList<String>, currentUserId: String) {
        val leaveOrDelete = if(listOfMembers[0] == currentUserId) {
                getString(R.string.delete_shopping_list)
            } else {
                getString(R.string.leave_shopping_list)
            }

        setFragmentResult(
            OpenedShoppingListViewModel.ARG_KEY_LEAVEDELETE,
            bundleOf(OpenedShoppingListViewModel.ARG_KEY_LEAVEDELETE to leaveOrDelete)
        )
        findNavController().popBackStack()
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = FindFriendsBottomSheetFragmentDirections.actionFindFriendsBottomSheetFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }

}