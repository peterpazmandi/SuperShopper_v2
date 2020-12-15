package com.inspirecoding.supershopper.ui.shoppinglists

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.auth.AuthRepository
import com.inspirecoding.supershopper.ui.register.RegisterViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShoppingListsViewModel @ViewModelInject constructor(
    private val authRepository: AuthRepository,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val user = state.getLiveData<User>("user")

    private val _shoppingListsFragmentsEventChannel = Channel<ShoppingListsFragmentsEvent>()
    val shoppingListsFragmentsEventChannel = _shoppingListsFragmentsEventChannel.receiveAsFlow()

    private val _currentUser = MutableLiveData<User>()
    val currentUser = state.getLiveData<User>("user")

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _shoppingListsFragmentsEventChannel.send(ShoppingListsFragmentsEvent.NavigateToSplashFragment)
        }
    }


    sealed class ShoppingListsFragmentsEvent {
        object NavigateToSplashFragment : ShoppingListsFragmentsEvent()
    }



}