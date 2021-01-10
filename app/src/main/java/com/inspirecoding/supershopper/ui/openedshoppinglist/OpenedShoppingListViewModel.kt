package com.inspirecoding.supershopper.ui.openedshoppinglist

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.shoppinglist.ShoppingListRepository
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class OpenedShoppingListViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    companion object {
        const val ARG_KEY_OPENEDSHOPPINGLIST = "openedShoppingList"
        const val ARG_KEY_DUEDATE = "dueDate"
        const val ARG_KEY_FRIENDSSHAREDWITH = "friendsSharedWith"
        const val ARG_KEY_LEAVEDELETE = "leaveDelete"
        const val ARG_KEY_TITLE = "title"
    }

    private val _listItemEventChannel = Channel<ListItemEvent>()
    val listItemEventChannel = _listItemEventChannel.receiveAsFlow()

    // ARGUMENTS
    val openedShoppingList = state.getLiveData<ShoppingList>(ARG_KEY_OPENEDSHOPPINGLIST)

    val currentUser = state.getLiveData<User>(ShoppingListsViewModel.ARG_KEY_USER)

//    private fun getFriends(shoppingList: ShoppingList) = liveData<ShoppingList> {
//        shoppingList.friendsSharedWith.forEach { userId ->
//            userRepository.getUserFromFirestore(userId).collect { result ->
//                when(result.status)
//                {
//                    SUCCESS -> {
//                        result.data?.let {
//                            println(shoppingList.usersSharedWith.size)
////                            shoppingList.usersSharedWith.add(it)
//                            if(shoppingList.usersSharedWith.size == shoppingList.friendsSharedWith.size) {
//                                println(shoppingList.usersSharedWith)
//                                emit(shoppingList)
//                            }
//                        }
//                    }
//                    LOADING -> onShowLoading()
//                    ERROR -> {
//                        result.message?.let {
//                            onShowErrorMessage(it)
//                        }
//                    }
//                }
//            }
//        }
//    }

    fun updateShoppingListDueDate(dueDate: Long) {
        openedShoppingList.value?.let { shoppingList ->
            shoppingList.dueDate = Date(dueDate)
            viewModelScope.launch {
                shoppingListRepository.updateShoppingListDueDate(
                    shoppingListId = shoppingList.shoppingListId,
                    dueDate = shoppingList.dueDate
                ).collect()
            }
        }
    }

    fun updateShoppingListsSharedWithFriends(friendsSharedWith: List<String>) {
        openedShoppingList.value?.let { shoppingList ->
            viewModelScope.launch {
                shoppingListRepository.updateShoppingListsSharedWithFriends(
                    shoppingListId = shoppingList.shoppingListId,
                    friendsSharedWith = friendsSharedWith
                ).collect()
            }
        }
    }

    fun updateShoppingListsTitle(newTitle: String) {
        openedShoppingList.value?.let { shoppingList ->
            viewModelScope.launch {
                shoppingListRepository.updateShoppingListTitle(
                    shoppingListId = shoppingList.shoppingListId,
                    title = newTitle
                ).collect{ result ->
                    when(result.status)
                    {
                        SUCCESS -> {
                            shoppingList.name = newTitle
                        }
                        LOADING -> {
                            onShowLoading()
                        }
                        ERROR -> {
                            result.message?.let {
                                onShowErrorMessage(it)
                            }
                        }
                    }

                }
            }
        }
    }

    fun leaveShoppingList() {
        openedShoppingList.value?.let { _shoppingList ->
            currentUser.value?.let { _currentUser ->
                _shoppingList.friendsSharedWith.remove(_currentUser.id)
                viewModelScope.launch {
                    shoppingListRepository.updateShoppingListsSharedWithFriends(
                        shoppingListId = _shoppingList.shoppingListId,
                        friendsSharedWith = _shoppingList.friendsSharedWith
                    ).collect { result ->
                        when (result.status) {
                            LOADING -> {
                            }
                            SUCCESS -> {
                                onNavigateBackWithoutResult()
                            }
                            ERROR -> {
                                result.message?.let {
                                    onShowErrorMessage(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun deleteShoppingList() {
        openedShoppingList.value?.let { _shoppingList ->
            viewModelScope.launch {
                shoppingListRepository.deleteShoppingList(
                    shoppingList = _shoppingList
                ).collect { result ->
                    when (result.status) {
                        LOADING -> {
                        }
                        SUCCESS -> {
                            onNavigateBackWithoutResult()
                        }
                        ERROR -> {
                            result.message?.let {
                                onShowErrorMessage(it)
                            }
                        }
                    }
                }
            }
        }
    }


    /** Events **/
    fun onNavigateToUpdateUserProfileBottomSheetFragment() {
        openedShoppingList.value?.let { _shoppingList ->
            viewModelScope.launch {
                _listItemEventChannel.send(ListItemEvent.NavigateToUpdateUserProfileBottomSheetFragment(_shoppingList.name))
            }
        }
    }
    private fun onNavigateBackWithoutResult() {
        viewModelScope.launch {
            _listItemEventChannel.send(ListItemEvent.NavigateBackWithoutResult)
        }
    }
    fun onShowLoading() {
        viewModelScope.launch {
            _listItemEventChannel.send(ListItemEvent.ShowLoading)
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _listItemEventChannel.send(ListItemEvent.ShowErrorMessage(message))
        }
    }


    sealed class ListItemEvent {
        data class NavigateToUpdateUserProfileBottomSheetFragment(val title: String) : ListItemEvent()
        object NavigateBackWithoutResult : ListItemEvent()
        object ShowLoading: ListItemEvent()
        data class ShowErrorMessage(val message: String) : ListItemEvent()
    }

}