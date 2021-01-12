package com.inspirecoding.supershopper.ui.findfriends

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.shoppinglist.ShoppingListRepository
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.categories.listitems.UserItem
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel
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

    private val listOfFriends = mutableListOf<User>()
    private val _listOfFriendsMLD = MutableLiveData<Resource<List<User>>>()
    val listOfFriendsLD: LiveData<Resource<List<User>>> = _listOfFriendsMLD

    fun getFriendsAlphabeticalList() {
        viewModelScope.launch {
            currentUser.value?.let { _currentUser ->
                userRepository.getFriendsAlphabeticalList(_currentUser).collect { result ->
                    when(result.status)
                    {
                        Status.LOADING -> {
                            _listOfFriendsMLD.postValue(Resource.Loading(true))
                        }
                        Status.SUCCESS -> {
                            result.data?.let { _listOfFriends ->
                                _listOfFriends.forEach { friend ->
                                    userRepository.getUserFromFirestore(friend.friendId).collect { result ->
                                        when (result.status) {
                                            Status.SUCCESS -> {
                                                result.data?.let { user ->
                                                    prepareLists(user)
                                                }
                                            }
                                            Status.LOADING -> {
                                                _listOfFriendsMLD.postValue(Resource.Loading(true))
                                            }
                                            Status.ERROR -> {
                                                result.message?.let { _message ->
                                                    onShowErrorMessage(_message)
                                                    _listOfFriendsMLD.postValue(Resource.Error(_message))
                                                }
                                            }
                                        }
                                    }
                                }
                                listOfSelectedUsers.add(_currentUser)
                                reorderListOfFriends()
                                _listOfFriendsMLD.postValue(Resource.Success(listOfFriends.toList()))
                            }
                        }
                        Status.ERROR -> {
                            result.message?.let {
                                onShowErrorMessage(it)
                                _listOfFriendsMLD.postValue(Resource.Error(it))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun prepareLists(user: User) {
        openedShoppingList.value?.let { _shoppingList ->
            if(_shoppingList.friendsSharedWith.contains(user.id)) {
                listOfSelectedUsers.add(user)
            } else {
                listOfFriends.add(user)
            }
        }
    }

    private fun reorderListOfFriends() {
        openedShoppingList.value?.let { _shoppingList ->
            if(_shoppingList.friendsSharedWith.size == listOfSelectedUsers.size) {
                _shoppingList.friendsSharedWith.forEachIndexed { index, friendId ->
                    val user = listOfSelectedUsers.find { user ->
                        user.id == friendId
                    }
                    listOfSelectedUsers.removeIf { user ->
                        user.id == friendId
                    }
                    user?.let {
                        listOfSelectedUsers.add(index, user)
                    }
                }
                println("listOfSelectedUsers -> ${listOfSelectedUsers.map { it.id }}" )
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