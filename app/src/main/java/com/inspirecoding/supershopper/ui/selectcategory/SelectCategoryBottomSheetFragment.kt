package com.inspirecoding.supershopper.ui.selectcategory

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
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.databinding.SelectCategoryBottomSheetFragmentBinding
import com.inspirecoding.supershopper.ui.addedititem.AddEditItemViewModel
import com.inspirecoding.supershopper.ui.categories.listitems.CategoryItem
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsFragmentDirections
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SelectCategoryBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel by viewModels<SelectCategoryBottomSheetViewModel>()
    private lateinit var binding : SelectCategoryBottomSheetFragmentBinding
    private lateinit var adapter: BaseListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SelectCategoryBottomSheetFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        setupCategoriesListObserver()
        viewModel.getListOfCategories()
        setupEvents()
    }

    private fun setupCategoriesListObserver() {
        viewModel.listOfCategories.observe(viewLifecycleOwner, { listOfCategories ->
            val categoryItems = createListOfCategoryItems(listOfCategories)

            adapter.submitList(categoryItems)
            adapter.notifyDataSetChanged()
        })
    }

    private fun initRecyclerView() {
        adapter = BaseListAdapter { _, selectedItem ->
            val selectedCategoryItem = selectedItem as CategoryItem
            val category = selectedCategoryItem.category
            viewModel.onNavigateBackWithResult(category)
        }

        binding.rvCategories.setHasFixedSize(true)
        binding.rvCategories.setItemViewCacheSize(50)

        binding.rvCategories.adapter = adapter
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.selectCategoryEvent.collect { event ->
                when(event)
                {
                    is SelectCategoryBottomSheetViewModel.SelectCategoryEvent.NavigateBackWithResult -> {
                        navigateBackWithResult(event.category)
                    }
                    is SelectCategoryBottomSheetViewModel.SelectCategoryEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)}
                }
            }
        }
    }

    private fun createListOfCategoryItems(listOfCategories: MutableList<Category>): MutableList<BaseItem<*>> {
        val newList = mutableListOf<BaseItem<*>>()

        for(i in 0 until listOfCategories.size) {
            val item = CategoryItem(listOfCategories[i])
            newList.add(item)
        }

        return newList
    }












    /** Navigation methods **/
    private fun navigateBackWithResult(category: Category) {
        setFragmentResult(
            AddEditItemViewModel.CATEGORY,
            bundleOf(AddEditItemViewModel.CATEGORY to category)
        )
        findNavController().popBackStack()
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = SelectCategoryBottomSheetFragmentDirections.actionSelectCategoryBottomSheetFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }

}