package com.inspirecoding.supershopper.ui.friends

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.*
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.friends.listitems.FriendsListItem
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status.*
import com.inspirecoding.supershopper.utils.baseclasses.BaseItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class FriendsViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    val currentUser = state.getLiveData<User>(ShoppingListsViewModel.ARG_KEY_USER)

    private val listOfUserObjects = mutableListOf<User>()
    private val _listOfFriends = MutableLiveData<Resource<List<User>>>()
    val listOfFriends: LiveData<Resource<List<User>>> = _listOfFriends

    private val _numberOfPendingFriendRequests = MutableLiveData<Int>()
    val numberOfPendingFriendRequests: LiveData<Int> = _numberOfPendingFriendRequests

    private val _friendsEvents = Channel<FriendsFragmentsEvent>()
    val friendsEvents = _friendsEvents.receiveAsFlow()

    fun getFriendsAlphabeticalList() {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                userRepository.getFriendsAlphabeticalList(user).collect { result ->
                    when(result.status)
                    {
                        LOADING -> {
                            _listOfFriends.postValue(Resource.Loading(true))
                        }
                        SUCCESS -> {
                            result.data?.let { listOfFriends ->
                                listOfFriends.forEach {
                                    userRepository.getUserFromFirestore(it.friendId).collect { result ->
                                        when (result.status) {
                                            LOADING -> {
                                                _listOfFriends.postValue(Resource.Loading(true))
                                            }
                                            SUCCESS -> {
                                                result.data?.let { user ->
                                                    listOfUserObjects.add(user)
                                                }
                                            }
                                            ERROR -> {
                                                result.message?.let { _message ->
                                                    onShowErrorMessage(_message)
                                                    _listOfFriends.postValue(Resource.Error(_message))
                                                }
                                            }
                                        }
                                    }
                                }
                                _listOfFriends.postValue(Resource.Success(listOfUserObjects))
                            }
                        }
                        ERROR -> {
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

    fun getListOfPendingFriendRequests() {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                userRepository.getListOfFriendRequests(user.id).collect { result ->
                    when(result.status)
                    {
                        SUCCESS -> {
                            result.data?.let { list ->
                                _numberOfPendingFriendRequests.postValue(list.size)
                            }
                        }
                        ERROR -> {
                            result.message?.let {
                                onShowErrorMessage(it)
                            }
                        }
                        LOADING -> { /** Don't do anything **/ }
                    }
                }
            }
        }
    }

    fun createListOfFriendsItem(listOfFriends: List<User>): MutableList<BaseItem<*>> {
        val listOfFriendsListItem = mutableListOf<BaseItem<*>>()
        listOfFriends.forEach {
            listOfFriendsListItem.add(FriendsListItem(it))
        }
        return listOfFriendsListItem
    }





    /** Events **/
    fun onRefreshList() {
        userRepository.clearLastResultOfFriends()
        listOfUserObjects.clear()

        getFriendsAlphabeticalList()
        getListOfPendingFriendRequests()
    }
    fun onSearchFriendSelected() {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                _friendsEvents.send(FriendsFragmentsEvent.NavigateToSearchFriendsFragment(user))
            }
        }
    }
    fun onUserProfileSelected(selectedUser: User) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                _friendsEvents.send(FriendsFragmentsEvent.NavigateToUserProfileFragment(user, selectedUser))
            }
        }
    }
    fun onFriendRequestsSelected() {
        viewModelScope.launch {
            currentUser.value?.let {
                _friendsEvents.send(FriendsFragmentsEvent.NavigateToFriendRequestsFragment(it))
            }
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _friendsEvents.send(FriendsFragmentsEvent.ShowErrorMessage(message))
        }
    }






    sealed class FriendsFragmentsEvent {
        data class NavigateToSearchFriendsFragment(val user: User): FriendsFragmentsEvent()
        data class NavigateToUserProfileFragment(val user: User, val selectedUser: User): FriendsFragmentsEvent()
        data class NavigateToFriendRequestsFragment(val user: User): FriendsFragmentsEvent()
        data class ShowErrorMessage(val message: String): FriendsFragmentsEvent()
    }

    override fun onCleared() {
        super.onCleared()
        userRepository.clearLastResultOfFriends()
    }

}