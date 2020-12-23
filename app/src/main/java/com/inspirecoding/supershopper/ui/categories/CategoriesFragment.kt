package com.inspirecoding.supershopper.ui.categories

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.databinding.CategoriesFragmentBinding
import com.inspirecoding.supershopper.ui.categories.listitems.CategoryItem
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesFragment : Fragment(R.layout.categories_fragment) {

    private val TAG = this.javaClass.simpleName

    private val viewModel by viewModels<CategoriesViewModel>()
    private lateinit var binding: CategoriesFragmentBinding
    private lateinit var adapter: BaseListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding = CategoriesFragmentBinding.bind(view)

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        initRecyclerView()
        viewModel.getListOfCategories()
        setupCategoriesListObserver()

        binding.ivCreateNew.setOnClickListener {

        }

    }

    private fun setupCategoriesListObserver() {
        viewModel.listOfCategories.observe(viewLifecycleOwner, { listOfCategories ->
            val categoryItems = createListOfCategoryItems(listOfCategories)
            Log.d(TAG, "observe -> ${categoryItems.map { (it as CategoryItem).category.position }}")

            adapter.submitList(categoryItems)

        })
    }

    private fun initRecyclerView() {
        adapter = BaseListAdapter { view, selectedItem ->
            (view as AppCompatImageView).let { imageView ->
                when(imageView.tag)
                {
                    getString(R.string.move_up) -> {
                        viewModel.onMoveItemUp(selectedItem.data as Category)
                    }
                    getString(R.string.move_down) -> {
                        viewModel.onMoveItemDown(selectedItem.data as Category)
                    }
                    getString(R.string.delete) -> {
                        viewModel.onRemoveItem(selectedItem.data as Category)
                    }
                }
            }
        }

        binding.rvCategories.adapter = adapter
    }

    private fun createListOfCategoryItems(listOfCategories: MutableList<Category>): MutableList<BaseItem<*>> {

        val newList = mutableListOf<BaseItem<*>>()

        listOfCategories.forEach {
            newList.add(CategoryItem(it))
        }

        return newList

    }

}