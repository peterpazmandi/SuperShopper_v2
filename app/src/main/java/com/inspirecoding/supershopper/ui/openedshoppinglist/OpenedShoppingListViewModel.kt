package com.inspirecoding.supershopper.ui.openedshoppinglist

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.notification.NotificationData
import com.inspirecoding.supershopper.notification.NotificationRepositoryImpl
import com.inspirecoding.supershopper.notification.PushNotification
import com.inspirecoding.supershopper.repository.shoppinglist.ShoppingListRepository
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class OpenedShoppingListViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val notificationRepositoryImpl: NotificationRepositoryImpl,
    @ApplicationContext private val appContext: Context,
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
                ).collect { result ->
                    when(result.status)
                    {
                        SUCCESS -> {
                            friendsSharedWith.forEach { userId ->
                                if (!shoppingList.friendsSharedWith.contains(userId)) {
                                    userRepository.getUserFromFirestore(userId)
                                        .collect { resultGetUser ->
                                            when (resultGetUser.status)
                                            {
                                                SUCCESS -> {
                                                    resultGetUser.data?.firebaseInstanceToken?.forEach { token ->
                                                        notificationRepositoryImpl.postNotification(
                                                            PushNotification(
                                                                data = NotificationData(
                                                                    title = appContext.getString(R.string.you_got_a_new_shopping_list),
                                                                    message = appContext.getString(
                                                                        R.string.shopping_list_has_been_shared_with_you,
                                                                        shoppingList.name
                                                                    )
                                                                ),
                                                                to = token
                                                            )
                                                        )
                                                    }
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


    fun leaveOrDeleteShoppingList(listOfUsers: ArrayList<User>) {
        currentUser.value?.let { _currentUser ->
            if(listOfUsers[0] == _currentUser) {
                listOfUsers.remove(_currentUser)
                deleteShoppingList(listOfUsers, _currentUser)
            } else {
                listOfUsers.remove(_currentUser)
                leaveShoppingList(listOfUsers)
            }
        }
    }
    private fun leaveShoppingList(listOfUsers: ArrayList<User>) {
        openedShoppingList.value?.let { _shoppingList ->
            currentUser.value?.let { _currentUser ->
                _shoppingList.friendsSharedWith.remove(_currentUser.id)
                viewModelScope.launch {
                    shoppingListRepository.updateShoppingListsSharedWithFriends(
                        shoppingListId = _shoppingList.shoppingListId,
                        friendsSharedWith = _shoppingList.friendsSharedWith
                    ).collect { result ->
                        when (result.status) {
                            SUCCESS -> {
                                listOfUsers.forEach { user ->
                                    user.firebaseInstanceToken.forEach { token ->
                                        notificationRepositoryImpl.postNotification(
                                            PushNotification(
                                                data = NotificationData(
                                                    title = appContext.getString(R.string.name_shopping_list, _shoppingList.name),
                                                    message = appContext.getString(
                                                        R.string.user_has_left_the_shopping_list,
                                                        _currentUser.name
                                                    )
                                                ),
                                                to = token
                                            )
                                        )
                                    }
                                }
                                onNavigateBackWithoutResult()
                            }
                            LOADING -> { onShowLoading() }
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

    private fun deleteShoppingList(listOfUsers: ArrayList<User>, currentUser: User) {
        openedShoppingList.value?.let { _shoppingList ->
            viewModelScope.launch {
                shoppingListRepository.deleteShoppingList(
                    shoppingList = _shoppingList
                ).collect { result ->
                    when (result.status) {
                        SUCCESS -> {
                            listOfUsers.forEach { user ->
                                user.firebaseInstanceToken.forEach { token ->
                                    notificationRepositoryImpl.postNotification(
                                        PushNotification(
                                            data = NotificationData(
                                                title = appContext.getString(R.string.delete_name_shopping_list, _shoppingList.name),
                                                message = appContext.getString(
                                                    R.string.user_has_deleted_the_shopping_list,
                                                    currentUser.name
                                                )
                                            ),
                                            to = token
                                        )
                                    )
                                }
                            }
                            onNavigateBackWithoutResult()
                        }
                        LOADING -> { onShowLoading() }
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