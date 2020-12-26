package com.inspirecoding.supershopper.ui.categories.addnew

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.AddNewCategoryFragmentBinding
import com.inspirecoding.supershopper.ui.categories.addnew.listitem.CategoryIconItem
import com.inspirecoding.supershopper.ui.register.RegisterFragmentDirections
import com.inspirecoding.supershopper.ui.register.RegisterViewModel
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import com.inspirecoding.supershopper.utils.dismissKeyboard
import com.inspirecoding.supershopper.utils.listOfCategoryIcons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddNewCategoryFragment : Fragment(R.layout.add_new_category_fragment) {

    private val viewModel by viewModels<AddNewCategoryViewModel>()
    private lateinit var binding : AddNewCategoryFragmentBinding
    private lateinit var adapter: BaseListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AddNewCategoryFragmentBinding.bind(view)

        initRecyclerView()
        setupAddNewCategoryEvents()

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvName.doAfterTextChanged {
            viewModel.name = it.toString().trim()
        }

        binding.ivSave.setOnClickListener {
            context?.dismissKeyboard(it)
            if (viewModel.areTheFieldsValid()) {
                viewModel.insertCategory()
                findNavController().popBackStack()
            }
        }
    }


    private fun initRecyclerView() {

        val listOfCategoryIconItems = createIconItems()

        adapter = BaseListAdapter { view, selectedItem ->
            viewModel.icon = selectedItem.data as Int
            adapter.currentList.forEach { baseItem ->
                (baseItem as CategoryIconItem).also { categoryIconItem ->
                    categoryIconItem.isSelected = false
                }
            }
            (selectedItem as CategoryIconItem).let {
                it.isSelected = true
                adapter.notifyDataSetChanged()
            }

        }



        binding.rvCategoryIcons.setHasFixedSize(true)
        binding.rvCategoryIcons.setItemViewCacheSize(100)

        val gridLayoutManager = GridLayoutManager(context, 5)
        binding.rvCategoryIcons.layoutManager = gridLayoutManager
        binding.rvCategoryIcons.adapter = adapter

        adapter.submitList(listOfCategoryIconItems)

    }

    private fun createIconItems(): List<CategoryIconItem> {

        val listOfCategoryItems = mutableListOf<CategoryIconItem>()

        listOfCategoryIcons.forEach {
            listOfCategoryItems.add(CategoryIconItem(it))
        }

        return listOfCategoryItems
    }



    private fun setupAddNewCategoryEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {

            viewModel.addNewCategoryEventChannel.collect { event ->
                when(event)
                {
                    is AddNewCategoryViewModel.AddNewCategoryEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }

        }
    }


    /** Navigation methods **/
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = AddNewCategoryFragmentDirections.actionAddNewCategoryFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }
}