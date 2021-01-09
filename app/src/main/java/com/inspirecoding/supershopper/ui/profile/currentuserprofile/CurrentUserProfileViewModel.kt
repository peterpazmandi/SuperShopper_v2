package com.inspirecoding.supershopper.ui.profile.currentuserprofile

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.Status.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CurrentUserProfileViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    companion object {
        val FIELD_TO_CHANGE = "fieldToChange"
        val USERNAME = "userName"
        val EMAIL = "email"
        val PASSWORD = "password"
    }

    val currentUser = state.getLiveData<User>(ShoppingListsViewModel.ARG_KEY_USER)

    private val _fragmentEvent = Channel<FragmentEvent>()
    val fragmentEvent = _fragmentEvent.receiveAsFlow()






    /** Events **/
    fun onChangeProfileImage(path: String) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                user.profilePicture = path
                userRepository.uploadProfilePictureOfUserToStorage(user).collect { result ->
                    when(result.status)
                    {
                        SUCCESS -> {
                            currentUser.postValue(result.data)
                            onShowResult()
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
    fun onChangeUserName(newUserName: String){
        viewModelScope.launch {
            currentUser.value?.let { user ->
                user.name = newUserName
                userRepository.updateNameOFUserInFirestore(user).collect { resultUpdateNameOFUserInFirestore ->
                    when(resultUpdateNameOFUserInFirestore.status)
                    {
                        SUCCESS -> {
                            userRepository.getAllFriends(user).collect { resultGetAllFriends ->
                                when(resultGetAllFriends.status)
                                {
                                    SUCCESS -> {
                                        resultGetAllFriends.data?.let { listOfFriends ->
                                            for(friend in listOfFriends) {
                                                userRepository.updateFriendName(friend.id, user.name).collect { resultFriend ->
                                                    when(resultFriend.status)
                                                    {
                                                        SUCCESS -> {
                                                            currentUser.postValue(user)
                                                        }
                                                        LOADING -> {
                                                            onShowLoading()
                                                        }
                                                        ERROR -> {
                                                            resultGetAllFriends.message?.let {
                                                                onShowErrorMessage(it)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        onShowResult()
                                    }
                                    LOADING -> {
                                        onShowLoading()
                                    }
                                    ERROR -> {
                                        resultGetAllFriends.message?.let {
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
                            resultUpdateNameOFUserInFirestore.message?.let {
                                onShowErrorMessage(it)
                            }
                        }
                    }

                }

            }
        }
    }
    fun onChangeEmailAddress(emailAddress: String){
        viewModelScope.launch {
            currentUser.value?.let { user ->
                user.emailAddress = emailAddress
                userRepository.updateCurrentUserEmail(user.emailAddress).collect { resultUpdateCurrentUserEmail ->
                    when(resultUpdateCurrentUserEmail.status)
                    {
                        SUCCESS -> {
                            currentUser.postValue(user)
                            onShowResult()
                        }
                        LOADING -> {
                            onShowLoading()
                        }
                        ERROR -> {
                            resultUpdateCurrentUserEmail.message?.let {
                                onShowErrorMessage(it)
                            }
                        }
                    }

                }

            }
        }
    }
    fun onChangePassword(password: String){
        viewModelScope.launch {
            currentUser.value?.let { user ->
                userRepository.updateCurrentUserPassword(password).collect { resultUpdateCurrentUserPassword ->
                    when(resultUpdateCurrentUserPassword.status)
                    {
                        SUCCESS -> {
                            currentUser.postValue(user)
                            onShowResult()
                        }
                        LOADING -> {
                            onShowLoading()
                        }
                        ERROR -> {
                            resultUpdateCurrentUserPassword.message?.let {
                                onShowErrorMessage(it)
                            }
                        }
                    }

                }

            }
        }
    }
    fun onLogOut() {
        viewModelScope.launch {
            userRepository.logOut()
            _fragmentEvent.send(FragmentEvent.LogOut)
        }
    }
    private fun onShowLoading() {
        viewModelScope.launch {
            currentUser.value?.let {
                _fragmentEvent.send(FragmentEvent.ShowLoading)
            }
        }
    }
    private fun onShowResult() {
        viewModelScope.launch {
            currentUser.value?.let {
                _fragmentEvent.send(FragmentEvent.ShowResult)
            }
        }
    }
    fun onNavigateToUpdateUserName() {
        viewModelScope.launch {
            _fragmentEvent.send(FragmentEvent.NavigateToUpdateUserName(USERNAME))
        }
    }
    fun onNavigateToUpdateEmailAddress() {
        viewModelScope.launch {
            _fragmentEvent.send(FragmentEvent.NavigateToUpdateEmailAddress(EMAIL))
        }
    }
    fun onNavigateToUpdatePassword() {
        viewModelScope.launch {
            _fragmentEvent.send(FragmentEvent.NavigateToUpdatePassword(PASSWORD))
        }
    }
    fun onNavigateBackWithCurrentUser() {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                _fragmentEvent.send(FragmentEvent.NavigateBackWithCurrentUser(user))
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
        data class NavigateToUpdateUserName(val fieldToChange: String): FragmentEvent()
        data class NavigateToUpdateEmailAddress(val fieldToChange: String): FragmentEvent()
        data class NavigateToUpdatePassword(val fieldToChange: String): FragmentEvent()
        object LogOut: FragmentEvent()
        object ShowResult: FragmentEvent()
        data class NavigateBackWithCurrentUser(val currentUser: User): FragmentEvent()
        data class ShowErrorMessage(val message: String): FragmentEvent()
    }
}