package com.inspirecoding.supershopper.ui.openedshoppinglist.items

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ListItem
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.databinding.OpenedShoppingListItemsFragmentBinding
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListFragmentDirections
import com.inspirecoding.supershopper.ui.openedshoppinglist.items.listitem.ListItemsItem
import com.inspirecoding.supershopper.utils.Status
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class OpenedShoppingListItemsFragment : Fragment(R.layout.opened_shopping_list_items_fragment) {

    private val TAG = this.javaClass.simpleName

    private val viewModel by viewModels<OpenedShoppingListItemsViewModel>()
    private lateinit var binding: OpenedShoppingListItemsFragmentBinding
    private lateinit var adapter: BaseListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = OpenedShoppingListItemsFragmentBinding.bind(view)

        initRecyclerView()
        setupShoppingListObserver()
        setupEventHandler()
        setupListItemsItemsObserver()

        binding.fabAddItem.setOnClickListener {
            viewModel.onAddItemFragment()
        }
    }

    private fun setupListItemsItemsObserver() {
        viewModel.listOfItems.observe(viewLifecycleOwner, { listOfSortedItems ->
            adapter.submitList(listOfSortedItems as List<BaseItem<*>>?)
            adapter.notifyDataSetChanged()
            binding.rvListOfItems.scrollToPosition(0)
        })
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
                    result.data?.let { shoppingList ->
                        setEmptyCartVisibility(shoppingList.listOfItems.isEmpty())
                        viewModel.createCategoryItem(shoppingList.listOfItems)
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

    private fun setEmptyCartVisibility(isListEmpty: Boolean) {
        if(isListEmpty) {
            binding.ivEmptyCartIllustration.makeItVisible()
            binding.tvEmptyCartText.makeItVisible()
            binding.tvAddItemText.makeItVisible()
        } else {
            binding.ivEmptyCartIllustration.makeItInVisible()
            binding.tvEmptyCartText.makeItInVisible()
            binding.tvAddItemText.makeItInVisible()
        }
    }

    private fun initRecyclerView() {
        adapter = BaseListAdapter { view, selectedItem ->
            if (view is AppCompatCheckBox) {
                (selectedItem as ListItemsItem).let { shoppingListItem ->
                    viewModel.openedShoppingList.value?.data?.let { shoppingList ->
                        itemCheckedChanged(view, shoppingListItem.listItem, shoppingList)
                    }
                }
            }

            if(view is ConstraintLayout) {
                viewModel.onEditItemFragment(selectedItem.data as ListItem)
            }
        }

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                binding.rvListOfItems.scrollToPosition(0)
            }
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                binding.rvListOfItems.scrollToPosition(0)
            }
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                binding.rvListOfItems.scrollToPosition(0)
            }
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.rvListOfItems.scrollToPosition(0)
            }
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                binding.rvListOfItems.scrollToPosition(0)
            }
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                binding.rvListOfItems.scrollToPosition(0)
            }
        })

        binding.rvListOfItems.setHasFixedSize(true)
        binding.rvListOfItems.setItemViewCacheSize(20)

        binding.rvListOfItems.adapter = adapter
    }

    private fun itemCheckedChanged(checkBox: AppCompatCheckBox, listItem: ListItem, shoppingList: ShoppingList) {
        val listOfItems: List<ListItem> = shoppingList.listOfItems

        listOfItems.map {
            if(it.id == listItem.id) {
                it.isBought = checkBox.isChecked
            }
        }
        viewModel.updateShoppingListItems(
            shoppingListId = shoppingList.shoppingListId,
            listOfItems
        )
    }





    private fun setupEventHandler() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.listItemEventChannel.collect { event ->
                when(event)
                {
                    is OpenedShoppingListItemsViewModel.ListItemEvent.NavigateToAddFragment ->  {
                        navigateToAddFragment(event.shoppingList)
                    }
                    is OpenedShoppingListItemsViewModel.ListItemEvent.NavigateToEditFragment ->  {
                        navigateToEditFragment(event.shoppingList, event.listItem)
                    }
                    is OpenedShoppingListItemsViewModel.ListItemEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }



    /** Navigation methods **/
    private fun navigateToAddFragment(shoppingList: ShoppingList) {
        val action = OpenedShoppingListFragmentDirections.actionOpenedShoppingListFragmentToAddEditItemFragment(shoppingList, null)
        findNavController().navigate(action)
    }
    private fun navigateToEditFragment(shoppingList: ShoppingList, listItem: ListItem) {
        val action = OpenedShoppingListFragmentDirections.actionOpenedShoppingListFragmentToAddEditItemFragment(shoppingList, listItem)
        findNavController().navigate(action)
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = OpenedShoppingListFragmentDirections.actionOpenedShoppingListFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
}