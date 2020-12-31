package com.inspirecoding.supershopper.ui.openedshoppinglist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.inspirecoding.supershopper.MainActivity
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.databinding.OpenedShoppingListFragmentBinding
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel.Companion.ARG_KEY_OPENEDSHOPPINGLIST
import com.inspirecoding.supershopper.ui.openedshoppinglist.adapter.OpenedShoppingListAdapter
import com.inspirecoding.supershopper.ui.openedshoppinglist.details.OpenedShoppingListDetailsFragment
import com.inspirecoding.supershopper.ui.openedshoppinglist.items.OpenedShoppingListItemsFragment
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsFragmentDirections
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class OpenedShoppingListFragment : Fragment(R.layout.opened_shopping_list_fragment) {

    // CONST
    private val TAG = this.javaClass.simpleName
    private val TAB_ITEMS_COUNT = 2

    private lateinit var binding: OpenedShoppingListFragmentBinding
    private val viewModel by viewModels<OpenedShoppingListViewModel>()
    private lateinit var openedShoppingListAdapter: OpenedShoppingListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = OpenedShoppingListFragmentBinding.bind(view)
        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        setupOpenedShoppingListObserver()
        setupTabSelectedListener()
        setupEventHandler()

        setFragmentResultListener(OpenedShoppingListViewModel.ARG_KEY_DUEDATE) { _, bundle ->
            bundle.getLong(OpenedShoppingListViewModel.ARG_KEY_DUEDATE).let { dueDate ->
                viewModel.updateShoppingListDueDate(dueDate)
            }
        }
    }

    private fun setupTabSelectedListener() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tabItem: TabLayout.Tab) {
                binding.viewPager.currentItem = tabItem.position
            }
            override fun onTabUnselected(tabItem: TabLayout.Tab?) {

            }
            override fun onTabReselected(tabItem: TabLayout.Tab?) {

            }
        })
    }

    private fun setupOpenedShoppingListObserver() {
        viewModel.openedShoppingList.observe(viewLifecycleOwner, { shoppingList ->
            binding.tvShoppingListName.text = shoppingList.name
            setupTabLayout(shoppingList)
        })
    }

    private fun setupTabLayout(shoppingList: ShoppingList) {
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.items)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.details)))

        openedShoppingListAdapter = OpenedShoppingListAdapter((activity as MainActivity), TAB_ITEMS_COUNT)

        val openedShoppingListItemsFragment = OpenedShoppingListItemsFragment()
        val openedShoppingListDetailsFragment = OpenedShoppingListDetailsFragment()
        val bundle = Bundle()
        bundle.putParcelable(ARG_KEY_OPENEDSHOPPINGLIST, shoppingList)
        bundle.putParcelable(ShoppingListsViewModel.ARG_KEY_USER, viewModel.currentUser.value)
        openedShoppingListItemsFragment.arguments = bundle
        openedShoppingListDetailsFragment.arguments = bundle

        openedShoppingListAdapter.addFragment(openedShoppingListItemsFragment)
        openedShoppingListAdapter.addFragment(openedShoppingListDetailsFragment)

        binding.viewPager.adapter = openedShoppingListAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when(position)
            {
                0 -> tab.text = getString(R.string.items)
                1 -> tab.text = getString(R.string.details)
            }
        }.attach()
    }



    private fun setupEventHandler() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.listItemEventChannel.collect { event ->
                when(event)
                {
                    is OpenedShoppingListViewModel.ListItemEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }



    /** Navigation methods **/
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = ShoppingListsFragmentDirections.actionShoppingListsFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }

























}