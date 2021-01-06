package com.inspirecoding.supershopper.ui.friends.pendingfriendsrequests

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.friends.listitems.FriendsListItem
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PendingFriendRequestsViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    val currentUser = state.getLiveData<User>(ShoppingListsViewModel.ARG_KEY_USER)
    val listOfPendingFriendRequests = currentUser.switchMap { _currentUser ->
        getListOfPendingRequests()
    }

    private val _fragmentEvents = Channel<FragmentEvent>()
    val fragmentEvents = _fragmentEvents.receiveAsFlow()

    private fun getListOfPendingRequests() = liveData {
        currentUser.value?.let { user ->
            userRepository.getListOfFriendRequests(user.id).collect { result ->
                when (result.status) {
                    SUCCESS -> {
                        result.data?.let { list ->
                            val usersList = mutableListOf<User>()
                            for (friendRequest in list) {
                                userRepository.getUserFromFirestore(friendRequest.requestOwnerId).collect { result ->
                                    when (result.status) {
                                        SUCCESS -> {
                                            result.data?.let { user ->
                                                usersList.add(user)

                                                if(list.size == usersList.size) {
                                                    emit(usersList)
                                                }
                                            }
                                        }
                                        LOADING -> onShowLoading()
                                        ERROR ->
                                            result.message?.let {
                                                onShowErrorMessage(it)
                                            }
                                    }
                                }
                            }
                        }
                    }
                    LOADING -> onShowLoading()
                    ERROR -> {
                        result.message?.let {
                            onShowErrorMessage(it)
                        }
                    }
                }
            }
        }
    }

    fun createSearchFriendsItemsListComparedToFriends(
        listOfFoundUsers: List<User>
    ): List<FriendsListItem> {
        val listOfSearchFriendItem = mutableListOf<FriendsListItem>()

        for (foundUser in listOfFoundUsers) {
            listOfSearchFriendItem.add(FriendsListItem(foundUser))
        }

        return listOfSearchFriendItem
    }


    /** Events **/
    fun onNavigateToProfileFragmentSelected(selectedUser: User) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                _fragmentEvents.send(
                    FragmentEvent.NavigateToUserProfileFragment(
                        user,
                        selectedUser
                    )
                )
            }
        }
    }
    private fun onShowLoading() {
        viewModelScope.launch {
            currentUser.value?.let {
                _fragmentEvents.send(FragmentEvent.ShowLoading)
            }
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _fragmentEvents.send(FragmentEvent.ShowErrorMessage(message))
        }
    }


    sealed class FragmentEvent {
        object ShowLoading: FragmentEvent()
        data class NavigateToUserProfileFragment(val user: User, val selectedUser: User) : FragmentEvent()
        data class ShowErrorMessage(val message: String) : FragmentEvent()
    }

    override fun onCleared() {
        super.onCleared()
        userRepository.clearLastResultOfFriends()
    }


}