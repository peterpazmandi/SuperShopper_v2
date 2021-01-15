package com.inspirecoding.supershopper.ui.openedshoppinglist.items

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ListItem
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.notification.NotificationData
import com.inspirecoding.supershopper.notification.NotificationRepositoryImpl
import com.inspirecoding.supershopper.notification.PushNotification
import com.inspirecoding.supershopper.repository.local.ShopperRepository
import com.inspirecoding.supershopper.repository.shoppinglist.ShoppingListRepository
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel.Companion.ARG_KEY_OPENEDSHOPPINGLIST
import com.inspirecoding.supershopper.ui.openedshoppinglist.items.listitem.ListItemsItem
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OpenedShoppingListItemsViewModel @ViewModelInject constructor(
    private val shopperRepository: ShopperRepository,
    private val userRepository: UserRepository,
    private val notificationRepositoryImpl: NotificationRepositoryImpl,
    @ApplicationContext private val appContext: Context,
    private val shoppingListRepository: ShoppingListRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val _listItemEventChannel = Channel<ListItemEvent>()
    val listItemEventChannel = _listItemEventChannel.receiveAsFlow()

    val openedShoppingList = state.getLiveData<ShoppingList>(ARG_KEY_OPENEDSHOPPINGLIST).switchMap {
        viewModelScope.launch {
            shopperRepository.getCategories().collect()
        }
        getShoppingListRealTime(it.shoppingListId)
    }
    val currentUser = state.getLiveData<User>(ShoppingListsViewModel.ARG_KEY_USER)

    private val _listOfItems = MutableLiveData<MutableList<ListItemsItem>>()
    val listOfItems: LiveData<MutableList<ListItemsItem>> = _listOfItems

    private fun getShoppingListRealTime(shoppingListId: String) = liveData {
        shoppingListRepository.getShoppingListRealTime(shoppingListId, viewModelScope).collect {  shoppingList ->
            when(shoppingList.status)
            {
                Status.SUCCESS -> {
                    shoppingList.data?.let { _shoppingList ->
                        _shoppingList.friendsSharedWith.map { friendId ->
                            userRepository.getUserFromFirestore(friendId).collect { userResult ->
                                when (userResult.status) {
                                    Status.SUCCESS -> {
                                        userResult.data?.let {
                                            _shoppingList.usersSharedWith.add(it)
                                        }
                                    }
                                    Status.LOADING -> {
                                        emit(Resource.Loading(true))
                                    }
                                    Status.ERROR -> {
                                        shoppingList.message?.let {
                                            emit(Resource.Error(it))
                                        }
                                    }
                                }
                            }
                        }

                        emit(Resource.Success(shoppingList.data))
                    }
                }
                Status.LOADING -> {
                    emit(Resource.Loading(true))
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
                { it.listItem.isBought },
                { it.category?.position },
                { it.listItem.item }
            )).toMutableList()

            println("listOfListItemsItem -> $listOfListItemsItem")
            _listOfItems.postValue(listOfListItemsItem)
        }
    }

    fun updateShoppingListItems(shoppingListId: String, listOfItems: List<ListItem>) {
        viewModelScope.launch {
            currentUser.value?.let { _currentUser ->
                openedShoppingList.value?.data?.let { shoppingList ->
                    val fulfilment = listOfItems.filter {
                        !it.isBought
                    }.size
                    if(fulfilment == 0) {
                        sendOutFinishedNotification(shoppingList, _currentUser)
                    }
                }
            }
            shoppingListRepository.updateShoppingListItems(shoppingListId, listOfItems).collect { result ->
                when(result.status)
                {
                    Status.SUCCESS -> { /** Don't do anything **/ }
                    Status.LOADING -> { /** Don't do anything **/ }
                    Status.ERROR -> {
                        result.message?.let {
                            onShowErrorMessage(it)
                        }
                    }
                }
            }
        }
    }
    private fun sendOutFinishedNotification(shoppingList: ShoppingList, currentUser: User) {
        shoppingList.usersSharedWith.forEach { user ->
            if(user != currentUser) {
                user.firebaseInstanceToken.forEach { token ->
                    notificationRepositoryImpl.postNotification(
                        PushNotification(
                            data = NotificationData(
                                title = appContext.getString(R.string.finished_shopping_list),
                                message = appContext.getString(R.string.shopping_list_has_been_completed, shoppingList.name)),
                            to = token)
                    )
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