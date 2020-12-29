package com.inspirecoding.supershopper.ui.addedititem

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.data.ListItem
import com.inspirecoding.supershopper.databinding.AddEditItemFragmentBinding
import com.inspirecoding.supershopper.ui.addedititem.AddEditItemViewModel.Companion.CATEGORY
import com.inspirecoding.supershopper.ui.addedititem.listitem.UnitItem
import com.inspirecoding.supershopper.ui.categories.addnew.listitem.CategoryIconItem
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsFragmentDirections
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import com.inspirecoding.supershopper.utils.listOfUnits
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditItemFragment : Fragment(R.layout.add_edit_item_fragment) {

    private val viewModel by viewModels<AddEditItemViewModel>()
    private lateinit var binding: AddEditItemFragmentBinding
    private lateinit var adapter: BaseListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = AddEditItemFragmentBinding.bind(view)

        initUnitRecyclerView()
        setupListItemObserverIfEdit()
        setupCategoryObserver()
        setupEventHandler()

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.etName.doAfterTextChanged {
            viewModel.item = it.toString().trim()
        }
        binding.etQuantity.doAfterTextChanged {
            viewModel.qunatity = it.toString().trim().toFloat()
        }

        binding.ivCategory.setOnClickListener {
            viewModel.onSelectCategory()
        }
        binding.tvCategory.setOnClickListener {
            viewModel.onSelectCategory()
        }

        setFragmentResultListener(CATEGORY) { _, bundle ->
            bundle.getParcelable<Category>(CATEGORY)?.let { result ->
                viewModel.setCategory(result)
            }
        }
    }

    private fun setupEventHandler() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditItemEventChannel.collect { event ->
                when(event)
                {
                    is AddEditItemViewModel.AddEditItemEvent.NavigateToSelectCategoryFragment -> {
                        navigateToSelectCategoryDialogFragment()
                    }
                    is AddEditItemViewModel.AddEditItemEvent.ShowErrorMessage ->  {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }

    private fun setupCategoryObserver() {
        viewModel.category.observe(viewLifecycleOwner, { category ->
            category.let {
                context?.let { _context ->
                    binding.ivCategory.setImageDrawable(ContextCompat.getDrawable(_context, category.iconDrawableResId))
                }

                if(category.nameStringResId != null) {
                    binding.tvCategory.text = getString(category.nameStringResId)
                } else {
                    binding.tvCategory.text = category.customName
                }
            }
        })
    }

    private fun initUnitRecyclerView() {

        val listOfUnitItems = createUnitItems()

        adapter = BaseListAdapter { _, selectedItem ->
            viewModel.unit = getString(selectedItem.data as Int)

            adapter.currentList.forEach { baseItem ->
                (baseItem as UnitItem).also { unitItem ->
                    unitItem.isSelected = false
                }
            }
            (selectedItem as UnitItem).let { unitItem ->
                unitItem.isSelected = true
                adapter.notifyDataSetChanged()
            }

        }

        binding.rvUnits.setHasFixedSize(true)
        binding.rvUnits.setItemViewCacheSize(50)

        val gridLayoutManager = GridLayoutManager(context, 3)
        binding.rvUnits.layoutManager = gridLayoutManager
        binding.rvUnits.adapter = adapter

        adapter.submitList(listOfUnitItems)
    }

    private fun createUnitItems(): List<UnitItem> {
        val listOfUnitItems = mutableListOf<UnitItem>()

        listOfUnits.forEach {
            listOfUnitItems.add(UnitItem(it))
        }

        return listOfUnitItems
    }

    private fun setupListItemObserverIfEdit() {
        viewModel.listItem.observe(viewLifecycleOwner, {
            it?.let {
                updateUiIfEditItem(it)
            }
        })
    }

    private fun updateUiIfEditItem(listItem: ListItem) {
        binding.tvAddEditText.text = getString(R.string.edit_item)

        binding.etName.setText(listItem.item)

        adapter.currentList.forEach { baseItem ->
            (baseItem as UnitItem).also { unitItem ->
                unitItem.isSelected = listItem.item == getString(unitItem.data)
            }
        }
        adapter.notifyDataSetChanged()

        binding.etQuantity.setText(listItem.qunatity.toString())

        listItem.categoryId?.let {
            viewModel.getCategoryById(it)
        }
    }











    /** Navigation methods **/
    private fun navigateToSelectCategoryDialogFragment() {
        val action = AddEditItemFragmentDirections.actionAddEditItemFragmentToSelectCategoryBottomSheetFragment()
        findNavController().navigate(action)
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = ShoppingListsFragmentDirections.actionShoppingListsFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }

}