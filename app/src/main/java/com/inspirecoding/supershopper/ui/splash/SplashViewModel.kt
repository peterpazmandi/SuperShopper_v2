package com.inspirecoding.supershopper.ui.splash

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.auth.AuthRepository
import com.inspirecoding.supershopper.ui.register.RegisterViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SplashViewModel @ViewModelInject constructor(
    private val authRepository: AuthRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    private val _splashEventChannel = Channel<SplashEvent>()
    val splashEventChannel = _splashEventChannel.receiveAsFlow()

    val userResource = authRepository.userResource

    fun checkUserLoggedIn() {
        viewModelScope.launch {
            authRepository.checkUserLoggedIn(this)
        }
    }

    /** Events **/
    fun onLoginSelected() {
        viewModelScope.launch {
            _splashEventChannel.send(SplashEvent.NavigateToLoginFragment)
        }
    }
    fun onRegistrationSelected() {
        viewModelScope.launch {
            _splashEventChannel.send(SplashEvent.NavigateToRegisterFragment)
        }
    }
    fun onNavigateToShoppingListsFragment(user: User) {
        viewModelScope.launch {
            _splashEventChannel.send(SplashEvent.NavigateToShoppingListsFragment(user))
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _splashEventChannel.send(SplashEvent.ShowErrorMessage(message))
        }
    }

    sealed class SplashEvent {
        object NavigateToLoginFragment : SplashEvent()
        object NavigateToRegisterFragment : SplashEvent()
        data class ShowErrorMessage(val message: String) : SplashEvent()
        data class NavigateToShoppingListsFragment(val user: User) : SplashEvent()
    }
}