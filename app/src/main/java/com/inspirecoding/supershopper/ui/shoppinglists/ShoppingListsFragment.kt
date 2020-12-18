package com.inspirecoding.supershopper.ui.shoppinglists

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.databinding.ShoppingListsFragmentBinding
import com.inspirecoding.supershopper.ui.shoppinglists.listitems.DateItem
import com.inspirecoding.supershopper.ui.shoppinglists.listitems.ShoppingListItemItem
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.getMonthLongName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ShoppingListsFragment : Fragment(R.layout.shopping_lists_fragment) {

    private val TAG = this.javaClass.simpleName

    private val viewModel by viewModels<ShoppingListsViewModel>()
    private lateinit var binding : ShoppingListsFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ShoppingListsFragmentBinding.bind(view)

        setupCurrentUserObserver()


        setupEvents()

        viewModel.shoppingLists.observe(viewLifecycleOwner, {
            Log.d(TAG, "$it")
        })
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.shoppingListsFragmentsEventChannel.collect { event ->
                when(event)
                {
                    ShoppingListsViewModel.ShoppingListsFragmentsEvent.NavigateToSplashFragment -> {
                        navigateToShoppingListsFragment()
                    }
                }
            }
        }
    }

    private fun setupCurrentUserObserver() {
        viewModel.currentUser.observe(viewLifecycleOwner, { _currentUser ->
            viewModel.getCurrentUserShoppingListsRealTime(_currentUser)
        })
    }

    private fun createShoppingListWithHeader(listOfShoppingLists: List<ShoppingList>) : MutableList<BaseItem<*>> {

        val listOfShoppingListsWithHeaders = mutableListOf<BaseItem<*>>()

        var currentHeader = ""

        listOfShoppingLists.forEach {  shoppingList ->
            val currentMonth = shoppingList.dueDate.getMonthLongName()
            if(currentHeader != currentMonth) {
                listOfShoppingListsWithHeaders.add(DateItem(shoppingList.dueDate))
                currentHeader = currentMonth
            } else {
                listOfShoppingListsWithHeaders.add(ShoppingListItemItem(shoppingList))
            }
        }
        return listOfShoppingListsWithHeaders
    }













    /** Navigation methods **/
    private fun navigateToShoppingListsFragment() {
        findNavController().navigate(R.id.action_shoppingListsFragment_to_splashFragment)
    }

}