package com.inspirecoding.supershopper.ui.shoppinglists

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.ShoppingListsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ShoppingListsFragment : Fragment(R.layout.shopping_lists_fragment) {

    private val viewModel by viewModels<ShoppingListsViewModel>()
    private lateinit var binding : ShoppingListsFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ShoppingListsFragmentBinding.bind(view)

        setupCurrentUserObserver()

        binding.textView.setOnClickListener {
            viewModel.signOut()
        }


        setupEvents()
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
            binding.textView.text = _currentUser.username
        })
    }


    /** Navigation methods **/
    private fun navigateToShoppingListsFragment() {
        findNavController().navigate(R.id.action_shoppingListsFragment_to_splashFragment)
    }

}