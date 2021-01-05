package com.inspirecoding.supershopper.ui.userprofile

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.FriendRequest
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status.*
import com.inspirecoding.supershopper.utils.enums.FriendshipStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*

class UserProfileViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName
    private val SELECTED_USER = "selectedUser"

    val currentUser = state.getLiveData<User>(ShoppingListsViewModel.ARG_KEY_USER)
    val selectedUser = state.getLiveData<User>(SELECTED_USER)

    private val _fragmentEvent = Channel<FragmentEvent>()
    val fragmentEvent = _fragmentEvent.receiveAsFlow()


    private val _friendshipStatus = combine(
        currentUser.asFlow(),
        selectedUser.asFlow()
    ) { currentUser, selectedUser ->
        println("currentUser $currentUser")
        println("selectedUser $selectedUser")
        combine(
            userRepository.getFriend(currentUser.id, selectedUser.id),
            userRepository.getFriendRequest(currentUser.id, selectedUser.id)
        ) { friend, friendRequest ->

            when(friend.status)
            {
                LOADING -> {
                    Resource.Loading(true)
                }
                SUCCESS -> {
                    if (friend.data != null) {
                        onShowResult(FriendshipStatus.FRIENDS)
                    } else {
                        when (friendRequest.status) {
                            LOADING -> {
                                Resource.Loading(true)
                            }
                            SUCCESS -> {
                                if (friendRequest.data != null) {
                                    val friendShipStatus = friendRequest.data.friendshipStatus
                                    when (friendShipStatus) {
                                        FriendshipStatus.SENDER.name -> {
                                            onShowResult(FriendshipStatus.SENDER)
                                        }
                                        FriendshipStatus.RECEIVER.name -> {
                                            onShowResult(FriendshipStatus.SENDER)
                                        }
                                        else -> {
                                            onShowResult(FriendshipStatus.NOFRIENDSHIP)
                                        }
                                    }
                                } else {
                                    onShowResult(FriendshipStatus.NOFRIENDSHIP)
                                }
                            }
                            ERROR -> {
                                friendRequest.message?.let {
                                    Resource.Error(it)
                                }
                            }
                        }
                    }
                }
                ERROR -> {
                    friend.message?.let {
                        Resource.Error(it)
                    }
                }
            }

        }.collect()
    }

    init {
        viewModelScope.launch {
            _friendshipStatus.collect()
        }
    }






    /** Events **/
    fun onSendFriendRequest() {
        viewModelScope.launch {
            currentUser.value?.let { _currentUser ->
                selectedUser.value?.let { _selectedUser ->
                    val senderFriendRequest = createSenderFriendRequest(
                        _currentUser.id, _selectedUser.id
                    )
                    val receiverFriendRequest = createReceiverFriendRequest(
                        _currentUser.id, _selectedUser.id
                    )
                    userRepository.sendFriendRequest(senderFriendRequest).collect { resultSender ->
                        when(resultSender.status)
                        {
                            LOADING -> {
                                onShowLoading()
                            }
                            SUCCESS -> {
                                userRepository.sendFriendRequest(receiverFriendRequest).collect { resultReceiver ->
                                    when(resultReceiver.status)
                                    {
                                        LOADING -> {
                                            onShowLoading()
                                        }
                                        SUCCESS -> {
                                            onShowResult(FriendshipStatus.SENDER)
                                        }
                                        ERROR -> {
                                            resultReceiver.message?.let {
                                                onShowErrorMessage(it)
                                            }
                                        }
                                    }
                                }
                            }
                            ERROR -> {
                                resultSender.message?.let {
                                    onShowErrorMessage(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun onUnfriend() {
        viewModelScope.launch {

        }
    }
    fun onAcceptFriendRequest() {
        viewModelScope.launch {

        }
    }
    fun onDeclineFriendRequest() {
        viewModelScope.launch {

        }
    }
    private fun onShowLoading() {
        viewModelScope.launch {
            currentUser.value?.let {
                _fragmentEvent.send(FragmentEvent.ShowLoading)
            }
        }
    }
    private fun onShowResult(friendShopStatus: FriendshipStatus) {
        viewModelScope.launch {
            currentUser.value?.let {
                _fragmentEvent.send(FragmentEvent.ShowResult(friendShopStatus))
            }
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _fragmentEvent.send(FragmentEvent.ShowErrorMessage(message))
        }
    }






    sealed class FragmentEvent {
        object ShowLoading: FragmentEvent()
        data class ShowResult(val friendShopStatus: FriendshipStatus): FragmentEvent()
        data class ShowErrorMessage(val message: String): FragmentEvent()
    }





    private fun createSenderFriendRequest(currentUserId: String, selectedUserId: String) = FriendRequest(
        friendshipStatus = FriendshipStatus.SENDER.name,
        requestDate = Date(),
        requestOwnerId = currentUserId,
        requestPartnerId = selectedUserId
    )
    private fun createReceiverFriendRequest(currentUserId: String, selectedUserId: String) = FriendRequest(
        friendshipStatus = FriendshipStatus.RECEIVER.name,
        requestDate = Date(),
        requestOwnerId = selectedUserId,
        requestPartnerId = currentUserId
    )
}