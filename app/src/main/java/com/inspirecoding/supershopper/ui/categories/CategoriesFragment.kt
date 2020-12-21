package com.inspirecoding.supershopper.ui.categories

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class CategoriesFragment : Fragment(R.layout.categories_fragment) {

    private val TAG = this.javaClass.simpleName

    private val viewModel by viewModels<CategoriesViewModel>()
    private lateinit var binding: CategoriesFragmentBinding
    private lateinit var adapter: BaseListAdapter


    private val itemTouchHelper_reOrder by lazy {

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                if (source.itemViewType != target.itemViewType) {
                    return false
                }

                val from = source.adapterPosition
                val to = target.adapterPosition
                viewModel.moveItem(from, to)
                adapter.onItemMove(from, to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val from = viewHolder.adapterPosition
                adapter.onItemDeleted(from)

            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if(actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)

                viewHolder.itemView.alpha = 1f
            }
        }


        ItemTouchHelper(simpleItemTouchCallback)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CategoriesFragmentBinding.bind(view)

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        initRecyclerView()
        viewModel.getListOfCategories()
        setupCategoriesListObserver()
        setupRemovedCategoriesObserver()
    }

    private fun setupRemovedCategoriesObserver() {
        viewModel.removedCategories.observe(viewLifecycleOwner, { _removedCategories ->
            if(_removedCategories.size > 0) {
                binding.ivUndo.makeItVisible()
            } else {
                binding.ivUndo.makeItInVisible()
            }
        })
    }

    private fun setupCategoriesListObserver() {
        viewModel.listOfCategories.observe(viewLifecycleOwner, { listOfCategories ->
            val categoryItems = createListOfCategoryItems(listOfCategories)

            adapter.setDataList(categoryItems)
            adapter.submitList(categoryItems)

        })
    }

    private fun initRecyclerView() {
        adapter = BaseListAdapter { view, selectedItem ->

        }

        binding.rvCategories.setHasFixedSize(true)
        binding.rvCategories.setItemViewCacheSize(30)
        binding.rvCategories.adapter = adapter

        itemTouchHelper_reOrder.attachToRecyclerView(binding.rvCategories)
    }

    private fun createListOfCategoryItems(listOfCategories: MutableList<Category>): MutableList<BaseItem<*>> {

        val newList = mutableListOf<BaseItem<*>>()

        listOfCategories.forEach {
            newList.add(CategoryItem(it))
        }

        return newList

    }

}