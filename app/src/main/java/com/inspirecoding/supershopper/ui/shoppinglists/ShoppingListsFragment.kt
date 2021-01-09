package com.inspirecoding.supershopper.ui.shoppinglists

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.databinding.ShoppingListsFragmentBinding
import com.inspirecoding.supershopper.ui.shoppinglists.listitems.DateItem
import com.inspirecoding.supershopper.ui.shoppinglists.listitems.ShoppingListItemItem
import com.inspirecoding.supershopper.utils.Status
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import com.inspirecoding.supershopper.utils.baseclasses.BaseListAdapter
import com.inspirecoding.supershopper.utils.getMonthLongName
import com.inspirecoding.supershopper.utils.makeItInVisible
import com.inspirecoding.supershopper.utils.makeItVisible
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ShoppingListsFragment : Fragment(R.layout.shopping_lists_fragment) {

    private val TAG = this.javaClass.simpleName

    private val viewModel by viewModels<ShoppingListsViewModel>()
    private lateinit var binding : ShoppingListsFragmentBinding

    private lateinit var adapter : BaseListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ShoppingListsFragmentBinding.bind(view)

        setupCurrentUserObserver()
        viewModel.getCurrentUserShoppingListsRealTime()
        setupRecyclerView()
        setupEvents()
        setupShoppingListObserver()

        binding.fabSettings.setOnClickListener {
            viewModel.onOpenSettings()
        }

        binding.fabCreateNewList.setOnClickListener {
            binding.motionLayout.setTransition(R.id.transitionStart, R.id.transitionEnd)
            binding.motionLayout.setTransitionDuration(200)
            binding.motionLayout.transitionToEnd()
        }
        binding.viewTransparentBackgroundFull.setOnClickListener {
            binding.motionLayout.setTransition(R.id.transitionEnd, R.id.transitionStart)
            binding.motionLayout.setTransitionDuration(200)
            binding.motionLayout.transitionToEnd()
        }
        binding.viewTransparentBackgroundHalf.setOnClickListener {
            /** Don't do anything **/
        }

        binding.tietName.doAfterTextChanged {
            viewModel.shoppingListName = it.toString().trim()
        }
        binding.tvCancel.setOnClickListener {
            binding.motionLayout.setTransition(R.id.transitionEnd, R.id.transitionStart)
            binding.motionLayout.setTransitionDuration(200)
            binding.motionLayout.transitionToEnd()
        }
        binding.tvCreate.setOnClickListener {
            if(viewModel.validateShoppingListName()) {
                viewModel.insertShoppingList()
            }
        }
        binding.fabFriends.setOnClickListener {
            viewModel.onOpenFriends()
        }

        binding.ivProfilePhoto.setOnClickListener {
            viewModel.onOpenCurrentUserProfile()
        }
        binding.tvUsername.setOnClickListener {
            viewModel.onOpenCurrentUserProfile()
        }

        setFragmentResultListener(ShoppingListsViewModel.ARG_KEY_USER) { _, bundle ->
            bundle.getParcelable<User>(ShoppingListsViewModel.ARG_KEY_USER)?.let { user ->
                viewModel.updateCurrentUser(user)
            }
        }
    }

    private fun setupShoppingListObserver() {
        viewModel.shoppingLists.observe(viewLifecycleOwner, { result ->
            when(result.status)
            {
                Status.LOADING -> {
                    binding.progressBar.makeItVisible()
                }
                Status.SUCCESS ->  {
                    binding.progressBar.makeItInVisible()

                    result.data?.let {
                        val list = createShoppingListWithHeader(it)
                        setUiIfListEmpty(list.size == 0)
                        adapter.submitList(list)
                    }

                }
                Status.ERROR ->  {
                    binding.progressBar.makeItInVisible()


                    result.message?.let { _errorMessage ->
                        viewModel.onShowErrorMessage(_errorMessage)
                    }

                }
            }
        })
    }

    private fun setUiIfListEmpty(isEmpty: Boolean) {

        if(isEmpty) {

            binding.ivEmptyCart.makeItVisible()
            binding.tvEmptyCart.makeItVisible()

        } else {

            binding.ivEmptyCart.makeItInVisible()
            binding.tvEmptyCart.makeItInVisible()

        }

    }

    private fun setupRecyclerView() {
        adapter = BaseListAdapter { view, selectedItem ->
            (selectedItem as ShoppingListItemItem).let { shoppingListItem ->
                val shoppingList = shoppingListItem.shoppingList
                viewModel.onOpenSelectedShoppingList(shoppingList)
            }
        }

        binding.rvShoppingLists.setHasFixedSize(true)
        binding.rvShoppingLists.setItemViewCacheSize(20)

        binding.rvShoppingLists.adapter = adapter

        binding.rvShoppingLists.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(!recyclerView.canScrollVertically(1)) {
                    viewModel.getCurrentUserShoppingListsRealTime()
                }
            }
        })
    }

    private fun setupEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.shoppingListsFragmentsEventChannel.collect { event ->
                when(event)
                {
                    ShoppingListsViewModel.ShoppingListsFragmentsEvent.NavigateToSettingsFragment -> {
                        navigateToSettingsFragment()
                    }
                    is ShoppingListsViewModel.ShoppingListsFragmentsEvent.NavigateToFriendsFragment -> {
                        navigateToFriendsListFragment(event.currentUser)
                    }
                    is ShoppingListsViewModel.ShoppingListsFragmentsEvent.OpenSelectedShoppingList -> {
                        navigateToOpenShoppingListsFragment(event.shoppingList)
                    }
                    is ShoppingListsViewModel.ShoppingListsFragmentsEvent.NavigateToCurrentUserProfileFragment -> {
                        navigateToCurrentUserProfileFragment(event.currentUser)
                    }
                    is ShoppingListsViewModel.ShoppingListsFragmentsEvent.ShowErrorMessage -> {
                        navigateToErrorBottomDialogFragment(event.message)
                    }
                }
            }
        }
    }

    private fun setupCurrentUserObserver() {
        viewModel.currentUser.observe(viewLifecycleOwner, { _currentUser ->
            setupLoggedInUsersProfile(_currentUser)
        })
    }

    private fun createShoppingListWithHeader(listOfShoppingLists: List<ShoppingList>) : MutableList<BaseItem<*>> {

        val listOfShoppingListsWithHeaders = mutableListOf<BaseItem<*>>()

        var currentHeader = ""

        listOfShoppingLists.forEach {  shoppingList ->
            val currentMonth = shoppingList.dueDate.getMonthLongName()
            if(currentHeader != currentMonth) {
                listOfShoppingListsWithHeaders.add(DateItem(shoppingList))
                currentHeader = currentMonth
            }
            listOfShoppingListsWithHeaders.add(ShoppingListItemItem(shoppingList))
        }
        return listOfShoppingListsWithHeaders
    }

    private fun setupLoggedInUsersProfile(user: User) {

        binding.tvUsername.text = user.name

        if(user.profilePicture.isNotEmpty()) {
            Picasso
                .get()
                .load(user.profilePicture)
                .fit()
                .placeholder(R.drawable.ic_default_profile_picture)
                .into(binding.ivProfilePhoto)
        }

    }











    /** Navigation methods **/
    private fun navigateToFriendsListFragment(currentUser: User) {
        val action = ShoppingListsFragmentDirections.actionShoppingListsFragmentToFriendsFragment(currentUser)
        findNavController().navigate(action)
    }
    private fun navigateToCurrentUserProfileFragment(currentUser: User) {
        val action = ShoppingListsFragmentDirections.actionShoppingListsFragmentToCurrentUserProfileFragment(currentUser)
        findNavController().navigate(action)
    }
    private fun navigateToSettingsFragment() {
        findNavController().navigate(R.id.action_shoppingListsFragment_to_settingsFragment)
    }
    private fun navigateToOpenShoppingListsFragment(shoppingList: ShoppingList) {
        viewModel.currentUser.value?.let { currentUser ->
            val action = ShoppingListsFragmentDirections.actionShoppingListsFragmentToOpenedShoppingListFragment(shoppingList, currentUser)
            findNavController().navigate(action)
        }
    }
    private fun navigateToErrorBottomDialogFragment(errorMessage: String) {
        val action = ShoppingListsFragmentDirections.actionShoppingListsFragmentToErrorBottomDialogFragment(errorMessage)
        findNavController().navigate(action)
    }

}