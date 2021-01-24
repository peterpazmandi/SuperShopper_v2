package com.inspirecoding.supershopper.ui.categories

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.databinding.CategoriesFragmentBinding
import com.inspirecoding.supershopper.ui.categories.listitems.CategorySettingsItem
import com.inspirecoding.supershopper.ui.settings.SettingsFragmentDirections
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class CategoriesFragment : Fragment(R.layout.categories_fragment) {

    private val TAG = this.javaClass.simpleName

    private val viewModel by viewModels<CategoriesViewModel>()
    private lateinit var binding: CategoriesFragmentBinding
    private lateinit var adapter: BaseListAdapter


    private val itemTouchHelper_reOrder by lazy {

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val from = source.adapterPosition
                val to = target.adapterPosition
                viewModel.moveItem(from, to)

                adapter.notifyItemMoved(from, to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val from = viewHolder.adapterPosition
                adapter.notifyItemRemoved(from)
                viewModel.onRemoveItem(from)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG)
                {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                viewHolder.itemView.alpha = 1.0f
                viewModel.updateItems()
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

        setupEvents()


//        binding.ivCreateNew.setOnClickListener {
//            viewModel.onAddCategorySelected()
//            viewModel.printLog()
//        }


        itemTouchHelper_reOrder.attachToRecyclerView(binding.rvCategories)
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.settingsEvents.collect { event ->
                when(event)
                {
                    CategoriesViewModel.CategoryEvent.NavigateToAddCategoryFragment -> {
                        navigateToAddCategoryFragment()
                    }
                    is CategoriesViewModel.CategoryEvent.NavigateToAddEditCategoryFragment -> {
                        navigateToEditCategoryFragment(event.category)
                    }
                    is CategoriesViewModel.CategoryEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }

    private fun setupCategoriesListObserver() {
        viewModel.listOfCategories.observe(viewLifecycleOwner, { listOfCategories ->
            val categoryItems = createListOfCategoryItems(listOfCategories)

            adapter.submitList(categoryItems)
            adapter.notifyDataSetChanged()

        })
    }

    private fun initRecyclerView() {
        adapter = BaseListAdapter { view, selectedItem ->
            if(view is AppCompatImageView) {
                (view as AppCompatImageView).let { imageView ->
                    when(imageView.tag)
                    {
                        getString(R.string.delete) -> {
                            val toDeleteItem = selectedItem.data as Category
                            adapter.notifyItemRemoved(toDeleteItem.position)
                            viewModel.onRemoveItem(toDeleteItem)
                        }
                    }
                }
            }
        }

        binding.rvCategories.setHasFixedSize(true)
        binding.rvCategories.setItemViewCacheSize(50)

        binding.rvCategories.adapter = adapter
    }

    private fun createListOfCategoryItems(listOfCategories: MutableList<Category>): MutableList<BaseItem<*>> {

        val newList = mutableListOf<BaseItem<*>>()

        for(i in 0 until listOfCategories.size) {

            val item = CategorySettingsItem(listOfCategories[i])

            newList.add(item)

        }

        return newList

    }



    /** Navigation methods **/
    private fun navigateToAddCategoryFragment() {
        val action = CategoriesFragmentDirections.actionCategoriesFragmentToAddNewCategoryFragment(null)
        findNavController().navigate(action)
    }
    private fun navigateToEditCategoryFragment(category: Category) {
        val action = CategoriesFragmentDirections.actionCategoriesFragmentToAddNewCategoryFragment(category)
        findNavController().navigate(action)
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = SettingsFragmentDirections.actionSettingsFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }

}