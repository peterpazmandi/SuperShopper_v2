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
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel
import com.inspirecoding.supershopper.ui.openedshoppinglist.details.OpenedShoppingListDetailsViewModel
import com.inspirecoding.supershopper.ui.selectduedate.SelectDueDateBottomSheetViewModel
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status
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

    private val usersSharedWith = arrayListOf<User>()
    private val _listOfFriends = MutableLiveData<Resource<List<User>>>()
    val listOfFriends: LiveData<Resource<List<User>>> = _listOfFriends

    fun getFriendsAlphabeticalList() {
        viewModelScope.launch {
            currentUser.value?.let { _currentUser ->
                usersSharedWith.add(_currentUser)
                val listOfUsers = arrayListOf<User>()
                userRepository.getFriendsAlphabeticalList(_currentUser).collect { result ->
                    when(result.status)
                    {
                        Status.SUCCESS ->  {

                            result.data?.let { listOfFriends ->
                                for (friend in listOfFriends) {
                                    openedShoppingList.value?.friendsSharedWith?.let { listOfFriendsIds ->

                                        if(!listOfFriendsIds.contains(friend.friendId)) {
                                            userRepository.getUserFromFirestore(friend.friendId).collect { result ->
                                                when(result.status)
                                                {
                                                    Status.SUCCESS -> {
                                                        result.data?.let { user ->
                                                            listOfUsers.add(user)
                                                            _listOfFriends.postValue(Resource.Success(listOfUsers))
                                                        }
                                                    }
                                                    Status.LOADING ->  {
                                                        _listOfFriends.postValue(Resource.Loading(true))
                                                    }
                                                    Status.ERROR -> {
                                                        result.message?.let {
                                                            _listOfFriends.postValue(Resource.Error(it))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Status.LOADING -> {
                            _listOfFriends.postValue(Resource.Loading(true))
                        }
                        Status.ERROR -> {
                            result.message?.let {
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
        usersSharedWith.add(user)
        println("addToListOfFriends - ${user}")
    }
    fun removeFromListOfFriends(user: User) {
        usersSharedWith.remove(user)
        println("removeFromListOfFriends - ${user}")
    }



    /** Events **/
    fun onNavigateBackWithResult() {
        viewModelScope.launch {
            val friendsIds = usersSharedWith.map {
                it.id
            } as ArrayList<String>
            println("${friendsIds}")
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

}