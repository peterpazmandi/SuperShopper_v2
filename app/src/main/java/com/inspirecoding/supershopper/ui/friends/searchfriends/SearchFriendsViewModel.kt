package com.inspirecoding.supershopper.ui.friends.searchfriends

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.Friend
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.friends.searchfriends.listitem.SearchFriendItem
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchFriendsViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName
    val currentUser = state.getLiveData<User>(ShoppingListsViewModel.ARG_KEY_USER)

    private val _searchFriendsEvents = Channel<SearchFriendsFragmentsEvent>()
    val searchFriendsEvents = _searchFriendsEvents.receiveAsFlow()

    fun searchFriend(searchText: String) {
        viewModelScope.launch {
            if(searchText.length > 4) {
                userRepository.searchFriends(searchText).collect { result ->
                    when(result.status)
                    {
                        LOADING -> {
                            onShowLoading()
                        }
                        SUCCESS -> {
                            result.data?.let { listOfFoundUsers ->
                                if(listOfFoundUsers.isEmpty()) {
                                    onNoUserFound()
                                } else {
                                    val listOfSearchFriendItem = createSearchFriendsItemsListComparedToFriends(listOfFoundUsers)
                                    onShowResult(listOfSearchFriendItem)
                                }
                            }
                        }
                        ERROR -> {
                            result.message?.let { _message ->
                                onShowErrorMessage(_message)
                            }
                        }
                    }
                }
            } else {
                onLessThenFourCharacters()
            }
        }
    }

    private fun createSearchFriendsItemsListComparedToFriends(
        listOfFoundUsers: List<User>
    ): List<SearchFriendItem> {
        val listOfSearchFriendItem = mutableListOf<SearchFriendItem>()

        for(foundUser in listOfFoundUsers) {
            listOfSearchFriendItem.add(SearchFriendItem(foundUser))
        }

        return listOfSearchFriendItem
    }







    /** Events **/
    private fun onLessThenFourCharacters() {
        viewModelScope.launch {
            currentUser.value?.let {
                _searchFriendsEvents.send(SearchFriendsFragmentsEvent.LessThenFiveCharacters)
            }
        }
    }
    private fun onNoUserFound() {
        viewModelScope.launch {
            currentUser.value?.let {
                _searchFriendsEvents.send(SearchFriendsFragmentsEvent.NoUserFound)
            }
        }
    }
    private fun onShowLoading() {
        viewModelScope.launch {
            currentUser.value?.let {
                _searchFriendsEvents.send(SearchFriendsFragmentsEvent.ShowLoading)
            }
        }
    }
    private fun onShowResult(listOfFriends: List<SearchFriendItem>) {
        viewModelScope.launch {
            currentUser.value?.let {
                _searchFriendsEvents.send(SearchFriendsFragmentsEvent.ShowResult(listOfFriends))
            }
        }
    }
    fun onNavigateToProfileFragmentSelected(selectedUser: User) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                _searchFriendsEvents.send(SearchFriendsFragmentsEvent.NavigateToProfileFragment(user, selectedUser))
            }
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _searchFriendsEvents.send(SearchFriendsFragmentsEvent.ShowErrorMessage(message))
        }
    }






    sealed class SearchFriendsFragmentsEvent {
        object LessThenFiveCharacters: SearchFriendsFragmentsEvent()
        object NoUserFound: SearchFriendsFragmentsEvent()
        object ShowLoading: SearchFriendsFragmentsEvent()
        data class ShowResult(val listOfFriends: List<SearchFriendItem>): SearchFriendsFragmentsEvent()
        data class NavigateToProfileFragment(val user: User, val selectedUser: User): SearchFriendsFragmentsEvent()
        data class ShowErrorMessage(val message: String): SearchFriendsFragmentsEvent()
    }

    override fun onCleared() {
        super.onCleared()
        userRepository.clearLastResultOfFriends()
    }
}