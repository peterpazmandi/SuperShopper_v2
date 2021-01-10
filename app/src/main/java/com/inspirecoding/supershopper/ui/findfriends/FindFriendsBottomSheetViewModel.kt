package com.inspirecoding.supershopper.ui.findfriends

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.Friend
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.shoppinglist.ShoppingListRepository
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.categories.listitems.UserItem
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel
import com.inspirecoding.supershopper.ui.openedshoppinglist.details.OpenedShoppingListDetailsViewModel
import com.inspirecoding.supershopper.ui.selectduedate.SelectDueDateBottomSheetViewModel
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class FindFriendsBottomSheetViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val _findFriendsEventChannel = Channel<FindFriendsEvent>()
    val findFriendsEventChannel = _findFriendsEventChannel.receiveAsFlow()

    val openedShoppingList = state.getLiveData<ShoppingList>(OpenedShoppingListViewModel.ARG_KEY_OPENEDSHOPPINGLIST)
    val currentUser = state.getLiveData<User>(ShoppingListsViewModel.ARG_KEY_USER)


    private val listOfSelectedUsers = mutableListOf<User>()

    private val listOfUserObjects = mutableListOf<User>()
    private val _listOfFriends = MutableLiveData<Resource<List<User>>>()
    val listOfFriends: LiveData<Resource<List<User>>> = _listOfFriends

    fun getFriendsAlphabeticalList() {
        viewModelScope.launch {
            currentUser.value?.let { _currentUser ->
                userRepository.getFriendsAlphabeticalList(_currentUser).collect { result ->
                    when(result.status)
                    {
                        Status.LOADING -> {
                            _listOfFriends.postValue(Resource.Loading(true))
                        }
                        Status.SUCCESS -> {
                            result.data?.let { listOfFriends ->
                                listOfFriends.forEach {
                                    if(_currentUser.id != it.friendId) {
                                        userRepository.getUserFromFirestore(it.friendId).collect { result ->
                                            when (result.status) {
                                                Status.SUCCESS -> {
                                                    result.data?.let { user ->
                                                        listOfUserObjects.add(user)
                                                    }
                                                }
                                                Status.LOADING -> {
                                                    _listOfFriends.postValue(Resource.Loading(true))
                                                }
                                                Status.ERROR -> {
                                                    result.message?.let { _message ->
                                                        onShowErrorMessage(_message)
                                                        _listOfFriends.postValue(Resource.Error(_message))
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        listOfSelectedUsers.add(_currentUser)
                                    }

                                }
                                _listOfFriends.postValue(Resource.Success(listOfUserObjects))
                            }
                        }
                        Status.ERROR -> {
                            result.message?.let {
                                onShowErrorMessage(it)
                                _listOfFriends.postValue(Resource.Error(it))
                            }
                        }
                    }
                }
            }
        }
    }

    fun clearLastResultOfFriends() {
        userRepository.clearLastResultOfFriends()
    }

    fun addToListOfFriends(user: User) {
        listOfSelectedUsers.add(user)
    }
    fun removeFromListOfFriends(user: User) {
        listOfSelectedUsers.remove(user)
    }



    /** Events **/
    fun onNavigateBackWithResult() {
        viewModelScope.launch {
            val friendsIds = listOfSelectedUsers.map {
                it.id
            } as ArrayList<String>

            _findFriendsEventChannel.send(FindFriendsEvent.NavigateBackWithResult(friendsIds))
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _findFriendsEventChannel.send(FindFriendsEvent.ShowErrorMessage(message))
        }
    }


    sealed class FindFriendsEvent {
        data class NavigateBackWithResult(val listOfFriends: ArrayList<String>) : FindFriendsEvent()
        data class ShowErrorMessage(val message: String) : FindFriendsEvent()
    }

    fun createListOfUserItems(listOfFriends: List<User>): MutableList<BaseItem<*>> {
        val listOfUserItems = mutableListOf<BaseItem<*>>()

        openedShoppingList.value?.let { shoppingList ->
            for (friend in listOfFriends) {
                val userItem = UserItem(friend)

                if(shoppingList.usersSharedWith.contains(friend)) {
                    listOfSelectedUsers.add(friend)
                    userItem.isSelected = true
                } else {

                    userItem.isSelected = false
                }
                listOfUserItems.add(userItem)
            }
        }

        return listOfUserItems
    }

}