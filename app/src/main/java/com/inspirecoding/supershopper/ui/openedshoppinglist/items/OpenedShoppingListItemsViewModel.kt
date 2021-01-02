package com.inspirecoding.supershopper.ui.openedshoppinglist.items

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.ListItem
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.local.ShopperRepository
import com.inspirecoding.supershopper.repository.shoppinglist.ShoppingListRepository
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel.Companion.ARG_KEY_OPENEDSHOPPINGLIST
import com.inspirecoding.supershopper.ui.openedshoppinglist.items.listitem.ListItemsItem
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OpenedShoppingListItemsViewModel @ViewModelInject constructor(
    private val shopperRepository: ShopperRepository,
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val _listItemEventChannel = Channel<ListItemEvent>()
    val listItemEventChannel = _listItemEventChannel.receiveAsFlow()

    val openedShoppingList = state.getLiveData<ShoppingList>(ARG_KEY_OPENEDSHOPPINGLIST).switchMap {
        getShoppingList(it.shoppingListId)
    }
    val currentUser = state.getLiveData<User>(ShoppingListsViewModel.ARG_KEY_USER)

    private val _listOfItems = MutableLiveData<MutableList<ListItemsItem>>()
    val listOfItems: LiveData<MutableList<ListItemsItem>> = _listOfItems

    private fun getShoppingList(shoppingListId: String) = liveData {
        shoppingListRepository.getShoppingListRealTime(shoppingListId, viewModelScope).collect {  shoppingList ->
            when(shoppingList.status)
            {
                Status.LOADING -> {
                    emit(Resource.Loading(true))
                }
                Status.SUCCESS -> {
                    emit(Resource.Success(shoppingList.data))
                }
                Status.ERROR ->  {
                    shoppingList.message?.let {
                        emit(Resource.Error(it))
                    }
                }
            }
        }
    }

    fun createCategoryItem(listOfItems: List<ListItem>) {
        viewModelScope.launch {
            val listOfListItemsItem = listOfItems.map { listItem ->
                listItem.categoryId?.let { categoryId ->
                    val category = shopperRepository.getCategoryByIdWithSuspend(categoryId)
                    val listItemsItem = ListItemsItem(listItem)
                    listItemsItem.category = category
                    return@map listItemsItem
                } ?: ListItemsItem(listItem)
            }.sortedWith(compareBy(
                { it.listItem.isBought},
                { it.category?.position},
                { it.listItem.item}
            )).toMutableList()

            _listOfItems.postValue(listOfListItemsItem)
        }
    }

    fun updateShoppingListItems(shoppingListId: String, listOfItems: List<ListItem>) {
        viewModelScope.launch {
            shoppingListRepository.updateShoppingListItems(shoppingListId, listOfItems).collect { result ->
                when(result.status)
                {
                    Status.LOADING -> { /** Don't do anything **/ }
                    Status.SUCCESS -> { /** Don't do anything **/ }
                    Status.ERROR -> {
                        result.message?.let {
                            onShowErrorMessage(it)
                        }
                    }
                }
            }
        }
    }



    /** Events **/
    fun onAddItemFragment() {
        viewModelScope.launch {
            openedShoppingList.value?.data?.let { shoppingList ->
                _listItemEventChannel.send(ListItemEvent.NavigateToAddFragment(shoppingList))
            }
        }
    }
    fun onEditItemFragment(listItem: ListItem) {
        viewModelScope.launch {
            openedShoppingList.value?.data?.let { shoppingList ->
                _listItemEventChannel.send(ListItemEvent.NavigateToEditFragment(shoppingList, listItem))
            }
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _listItemEventChannel.send(ListItemEvent.ShowErrorMessage(message))
        }
    }


    sealed class ListItemEvent {
        data class NavigateToAddFragment(val shoppingList: ShoppingList) : ListItemEvent()
        data class NavigateToEditFragment(val shoppingList: ShoppingList, val listItem: ListItem) : ListItemEvent()
        data class ShowErrorMessage(val message: String) : ListItemEvent()
    }

}