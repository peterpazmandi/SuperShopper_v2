package com.inspirecoding.supershopper.ui.openedshoppinglist.details

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.ListItem
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.shoppinglist.ShoppingListRepository
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OpenedShoppingListDetailsViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val _listDetailsEventChannel = Channel<ListDetailsEvent>()
    val listItemEventChannel = _listDetailsEventChannel.receiveAsFlow()

    val openedShoppingList = state.getLiveData<ShoppingList>(OpenedShoppingListViewModel.ARG_KEY_OPENEDSHOPPINGLIST).switchMap {
        getShoppingList(it.shoppingListId)
    }
    val currentUser = state.getLiveData<User>(ShoppingListsViewModel.ARG_KEY_USER)


    private fun getShoppingList(shoppingListId: String) = liveData<Resource<ShoppingList>> {
        shoppingListRepository.getShoppingListRealTime(shoppingListId, viewModelScope).collect {  shoppingList ->

            when(shoppingList.status)
            {
                Status.LOADING -> {
                    emit(Resource.Loading(true))
                }
                Status.SUCCESS -> {
                    shoppingList.data?.friendsSharedWith?.map { friendId ->
                        userRepository.getUserFromFirestore(friendId).collect { userResult ->
                            when (userResult.status) {
                                Status.LOADING -> {
                                    emit(Resource.Loading(true))
                                }
                                Status.SUCCESS -> {
                                    userResult.data?.let {
                                        shoppingList.data.usersSharedWith.add(it)
                                        if (shoppingList.data.usersSharedWith.size == shoppingList.data.friendsSharedWith.size) {
                                            emit(Resource.Success(shoppingList.data))
                                        }
                                    }
                                }
                                Status.ERROR -> {
                                    shoppingList.message?.let {
                                        emit(Resource.Error(it))
                                    }
                                }
                            }
                        }
                    }
                }
                Status.ERROR ->  {
                    shoppingList.message?.let {
                        emit(Resource.Error(it))
                    }
                }
            }
        }
    }





    /** Events **/
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _listDetailsEventChannel.send(ListDetailsEvent.ShowErrorMessage(message))
        }
    }


    sealed class ListDetailsEvent {
        data class ShowErrorMessage(val message: String) : ListDetailsEvent()
    }

}