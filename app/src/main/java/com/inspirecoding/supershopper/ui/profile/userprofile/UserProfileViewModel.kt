package com.inspirecoding.supershopper.ui.profile.userprofile

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.Friend
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

    private var friendOwnerCurrentUser: Friend? = null
    private var friendOwnerSelectedUser: Friend? = null
    private var senderFriendRequest: FriendRequest? = null
    private var receiverFriendRequest: FriendRequest? = null

    private val _friendshipStatus = combine(
        currentUser.asFlow(),
        selectedUser.asFlow()
    ) { currentUser, selectedUser ->
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
                        this.friendOwnerCurrentUser = friend.data
                        userRepository.getFriend(selectedUser.id, currentUser.id).collect { _friendOwnerSelectedUser ->
                            when(_friendOwnerSelectedUser.status)
                            {
                                SUCCESS -> {
                                    friendOwnerSelectedUser = _friendOwnerSelectedUser.data
                                }
                                LOADING -> {
                                    Resource.Loading(true)
                                }
                                ERROR -> {
                                    _friendOwnerSelectedUser.message?.let {
                                        onShowErrorMessage(it)
                                    }
                                }
                            }
                        }
                        onShowResult(FriendshipStatus.FRIENDS)
                    } else {
                        when (friendRequest.status) {
                            SUCCESS -> {
                                if (friendRequest.data != null) {
                                    when (friendRequest.data.friendshipStatus) {

                                        FriendshipStatus.SENDER.name -> {
                                            senderFriendRequest = friendRequest.data

                                            userRepository.getFriendRequest(
                                                selectedUser.id,
                                                currentUser.id
                                            ).collect { resultReceiver ->
                                                when (resultReceiver.status) {
                                                    SUCCESS -> {
                                                        receiverFriendRequest = resultReceiver.data
                                                    }
                                                    LOADING -> {
                                                        Resource.Loading(true)
                                                    }
                                                    ERROR -> {
                                                        resultReceiver.message?.let {
                                                            onShowErrorMessage(it)
                                                        }
                                                    }
                                                }
                                            }

                                            onShowResult(FriendshipStatus.SENDER)
                                        }

                                        FriendshipStatus.RECEIVER.name -> {
                                            receiverFriendRequest = friendRequest.data

                                            userRepository.getFriendRequest(
                                                selectedUser.id,
                                                currentUser.id
                                            ).collect { resultSender ->
                                                when (resultSender.status) {
                                                    SUCCESS -> {
                                                        senderFriendRequest = resultSender.data
                                                    }
                                                    LOADING -> {
                                                        Resource.Loading(true)
                                                    }
                                                    ERROR -> {
                                                        resultSender.message?.let {
                                                            onShowErrorMessage(it)
                                                        }
                                                    }
                                                }
                                            }

                                            onShowResult(FriendshipStatus.RECEIVER)
                                        }

                                        else -> {
                                            onShowResult(FriendshipStatus.NOFRIENDSHIP)
                                        }

                                    }
                                } else {
                                    onShowResult(FriendshipStatus.NOFRIENDSHIP)
                                }
                            }
                            LOADING -> {
                                Resource.Loading(true)
                            }
                            ERROR -> {
                                friendRequest.message?.let {
                                    onShowErrorMessage(it)
                                }
                            }
                        }
                    }
                }
                ERROR -> {
                    friend.message?.let {
                        onShowErrorMessage(it)
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
                    senderFriendRequest = createSenderFriendRequest(
                        _currentUser.id, _selectedUser.id
                    )
                    receiverFriendRequest = createReceiverFriendRequest(
                        _currentUser.id, _selectedUser.id
                    )
                    userRepository.sendFriendRequest(senderFriendRequest as FriendRequest).collect { resultSender ->
                        when(resultSender.status)
                        {
                            LOADING -> {
                                onShowLoading()
                            }
                            SUCCESS -> {
                                userRepository.sendFriendRequest(receiverFriendRequest as FriendRequest).collect { resultReceiver ->
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

    fun onAcceptFriendRequest() {
        viewModelScope.launch {
            if(senderFriendRequest != null && receiverFriendRequest != null) {
                userRepository.removeFriendRequest(senderFriendRequest as FriendRequest).collect { resultSender ->
                    when(resultSender.status)
                    {
                        LOADING -> {
                            onShowLoading()
                        }
                        ERROR -> {
                            resultSender.message?.let {
                                onShowErrorMessage(it)
                            }
                        }
                        SUCCESS -> {
                            userRepository.removeFriendRequest(receiverFriendRequest as FriendRequest).collect { resultReceiver ->
                                when(resultReceiver.status)
                                {
                                    LOADING -> {
                                        onShowLoading()
                                    }
                                    ERROR -> {
                                        resultReceiver.message?.let {
                                            onShowErrorMessage(it)
                                        }
                                    }
                                    SUCCESS -> {
                                        currentUser.value?.let { _currentUser ->
                                            selectedUser.value?.let { _selectedUser ->
                                                createFriend(_currentUser, _selectedUser).let { friend ->
                                                    userRepository.createFriend(friend).collect { resultFriend ->
                                                        when(resultFriend.status)
                                                        {
                                                            LOADING -> {
                                                                onShowLoading()
                                                            }
                                                            ERROR -> {
                                                                resultReceiver.message?.let {
                                                                    onShowErrorMessage(it)
                                                                }
                                                            }
                                                            SUCCESS -> {
                                                                createFriend(_selectedUser, _currentUser).let { friend ->
                                                                    userRepository.createFriend(friend).collect { resultFriend ->
                                                                        when(resultFriend.status)
                                                                        {
                                                                            LOADING -> {
                                                                                onShowLoading()
                                                                            }
                                                                            ERROR -> {
                                                                                resultReceiver.message?.let {
                                                                                    onShowErrorMessage(it)
                                                                                }
                                                                            }
                                                                            SUCCESS -> {
                                                                                onShowResult(FriendshipStatus.FRIENDS)
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
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
            friendOwnerCurrentUser?.let { _friendOwnerCurrentUser ->
                friendOwnerSelectedUser?.let { _friendOwnerSelectedUser ->
                    userRepository.removeFriend(_friendOwnerCurrentUser).collect { resultCurrentUser ->
                        when(resultCurrentUser.status)
                        {
                            SUCCESS -> {
                                userRepository.removeFriend(_friendOwnerSelectedUser).collect { resultSelectedUser ->
                                    when(resultSelectedUser.status)
                                    {
                                        SUCCESS -> {
                                            onShowResult(FriendshipStatus.NOFRIENDSHIP)
                                        }
                                        LOADING -> {
                                            onShowLoading()
                                        }
                                        ERROR -> {
                                            resultCurrentUser.message?.let {
                                                onShowErrorMessage(it)
                                            }
                                        }
                                    }
                                }
                            }
                            LOADING -> {
                                onShowLoading()
                            }
                            ERROR -> {
                                resultCurrentUser.message?.let {
                                    onShowErrorMessage(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    fun onRemoveFriendRequest() {
        viewModelScope.launch {
            viewModelScope.launch {
                viewModelScope.launch {
                    if(senderFriendRequest != null && receiverFriendRequest != null) {
                        userRepository.removeFriendRequest(senderFriendRequest as FriendRequest).collect { resultSender ->
                            when(resultSender.status)
                            {
                                SUCCESS -> {
                                    userRepository.removeFriendRequest(receiverFriendRequest as FriendRequest).collect { resultReceiver ->
                                        when(resultReceiver.status)
                                        {
                                            LOADING -> {
                                                onShowLoading()
                                            }
                                            ERROR -> {
                                                resultReceiver.message?.let {
                                                    onShowErrorMessage(it)
                                                }
                                            }
                                            SUCCESS -> {
                                                onShowResult(FriendshipStatus.NOFRIENDSHIP)
                                            }
                                        }
                                    }
                                }
                                LOADING -> {
                                    onShowLoading()
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




    private fun createFriend(friend: User, friendshipOwner: User): Friend {
        return Friend(
            id = UUID.randomUUID().toString(),
            friendId = friend.id,
            friendName = friend.name,
            friendshipOwnerId = friendshipOwner.id
        )
    }
    private fun createSenderFriendRequest(currentUserId: String, selectedUserId: String) = FriendRequest(
        id = UUID.randomUUID().toString(),
        friendshipStatus = FriendshipStatus.SENDER.name,
        requestDate = Date(),
        requestOwnerId = currentUserId,
        requestPartnerId = selectedUserId
    )
    private fun createReceiverFriendRequest(currentUserId: String, selectedUserId: String) = FriendRequest(
        id = UUID.randomUUID().toString(),
        friendshipStatus = FriendshipStatus.RECEIVER.name,
        requestDate = Date(),
        requestOwnerId = selectedUserId,
        requestPartnerId = currentUserId
    )
}