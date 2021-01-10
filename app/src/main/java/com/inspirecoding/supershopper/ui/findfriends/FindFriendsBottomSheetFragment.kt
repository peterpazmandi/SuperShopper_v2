package com.inspirecoding.supershopper.ui.findfriends

import android.app.Dialog
import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.UserInfo
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.FindFriendsBottomSheetFragmentBinding
import com.inspirecoding.supershopper.ui.categories.listitems.UserItem
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListFragmentDirections
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel.Companion.ARG_KEY_FRIENDSSHAREDWITH
import com.inspirecoding.supershopper.ui.openedshoppinglist.details.OpenedShoppingListDetailsViewModel
import com.inspirecoding.supershopper.utils.Status
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class FindFriendsBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: FindFriendsBottomSheetViewModel by viewModels()
    private lateinit var binding: FindFriendsBottomSheetFragmentBinding

    private lateinit var adapter : BaseListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FindFriendsBottomSheetFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEventHandler()
        initRecyclerView()
        viewModel.getFriendsAlphabeticalList()
        setupFriendsListObserver()

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnAddFriends.setOnClickListener {
            viewModel.onNavigateBackWithResult()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.clearLastResultOfFriends()
    }

    private fun setupFriendsListObserver() {
        viewModel.listOfFriends.observe(viewLifecycleOwner, { result ->
            when (result.status)
            {
                Status.LOADING -> {
                    binding.progressBar.makeItVisible()
                }
                Status.SUCCESS -> {
                    result.data?.let { listOfFriends ->
                        val listOfUserItems = viewModel.createListOfUserItems(listOfFriends)
                        adapter.submitList(listOfUserItems)
                    }
                    binding.progressBar.makeItInVisible()
                }
                Status.ERROR -> {
                    result.message?.let {
                        viewModel.onShowErrorMessage(it)
                    }
                    binding.progressBar.makeItInVisible()
                }
            }
        })
    }

    private fun initRecyclerView() {

        adapter = BaseListAdapter { _, selectedItem ->
            val _selectedItem = (selectedItem as UserItem)
            if(_selectedItem.isSelected) {
                viewModel.addToListOfFriends(_selectedItem.user)
            } else {
                viewModel.removeFromListOfFriends(_selectedItem.user)
            }
        }

        binding.rvUsersList.adapter = adapter

        binding.rvUsersList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(!recyclerView.canScrollVertically(1)) {
                    viewModel.getFriendsAlphabeticalList()
                }
            }
        })
    }

    private fun setupEventHandler() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.findFriendsEventChannel.collect { event ->
                when(event)
                {
                    is FindFriendsBottomSheetViewModel.FindFriendsEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                    is FindFriendsBottomSheetViewModel.FindFriendsEvent.NavigateBackWithResult -> {
                        navigateBackWithResult(event.listOfFriends)
                    }
                }
            }
        }
    }



    /** Navigation methods **/
    private fun navigateBackWithResult(listOfFriends: ArrayList<String>) {
        setFragmentResult(
            ARG_KEY_FRIENDSSHAREDWITH,
            bundleOf(ARG_KEY_FRIENDSSHAREDWITH to listOfFriends)
        )
        findNavController().popBackStack()
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = FindFriendsBottomSheetFragmentDirections.actionFindFriendsBottomSheetFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
}