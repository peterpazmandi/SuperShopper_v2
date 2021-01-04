package com.inspirecoding.supershopper.ui.friends

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.*
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.friends.listitems.FriendsListItem
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status
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
    val listOfFriends = currentUser.switchMap { _user ->
        getFriendsAlphabeticalList()
    }

    private val _friendsEvents = Channel<FriendsFragmentsEvent>()
    val friendsEvents = _friendsEvents.receiveAsFlow()

    fun getFriendsAlphabeticalList() = liveData<Resource<List<User>>> {
        currentUser.value?.let { currentUser ->
            userRepository.getFriendsAlphabeticalList(currentUser).collect { result ->
                when(result.status)
                {
                    LOADING -> {
                        emit(Resource.Loading(true))
                    }
                    SUCCESS -> {
                        result.data?.let { listOfFriends ->
                            listOfFriends.forEach {
                                userRepository.getUserFromFirestore(it.friendId).collect { result ->
                                    when (result.status) {
                                        LOADING -> {
                                            emit(Resource.Loading(true))
                                        }
                                        SUCCESS -> {
                                            result.data?.let { user ->
                                                listOfUserObjects.add(user)
                                            }
                                        }
                                        ERROR -> {
                                            result.message?.let { _message ->
                                                onShowErrorMessage(_message)
                                                emit(Resource.Error(_message))
                                            }
                                        }
                                    }
                                }
                            }
                            emit(Resource.Success(listOfUserObjects))
                        }
                    }
                    ERROR -> {
                        result.message?.let {
                            onShowErrorMessage(it)
                            emit(Resource.Error(it))
                        }
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
    fun onSearchFriendSelected() {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                _friendsEvents.send(FriendsFragmentsEvent.NavigateSearchFriendsFragment(user))
            }
        }
    }
    fun onFriendRequestsSelected() {
        viewModelScope.launch {
            currentUser.value?.let {
                // TODO: Get list of friend requests
//                _friendsEvents.send(FriendsFragmentsEvent.NavigateFriendRequestsFragment(it))
            }
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _friendsEvents.send(FriendsFragmentsEvent.ShowErrorMessage(message))
        }
    }






    sealed class FriendsFragmentsEvent {
        data class NavigateSearchFriendsFragment(val user: User): FriendsFragmentsEvent()
        data class NavigateFriendRequestsFragment(val listOfFriendRequests: List<FriendRequest>): FriendsFragmentsEvent()
        data class ShowErrorMessage(val message: String): FriendsFragmentsEvent()
    }

    override fun onCleared() {
        super.onCleared()
        userRepository.clearLastResultOfFriends()
    }

}