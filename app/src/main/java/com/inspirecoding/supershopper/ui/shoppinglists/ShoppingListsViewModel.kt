package com.inspirecoding.supershopper.ui.shoppinglists

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.shoppinglist.ShoppingListRepository
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.utils.Status
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ShoppingListsViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val user = state.getLiveData<User>("user")

    private val _shoppingListsFragmentsEventChannel = Channel<ShoppingListsFragmentsEvent>()
    val shoppingListsFragmentsEventChannel = _shoppingListsFragmentsEventChannel.receiveAsFlow()

    private val _currentUser = MutableLiveData<User>()
    val currentUser = state.getLiveData<User>("user")

    private val _shoppingLists = MutableLiveData<Resource<ShoppingList>>()
    val shoppingLists : LiveData<Resource<ShoppingList>> = _shoppingLists

    fun signOut() {
        viewModelScope.launch {
            userRepository.signOut()
            _shoppingListsFragmentsEventChannel.send(ShoppingListsFragmentsEvent.NavigateToSplashFragment)
        }
    }

    fun getCurrentUserShoppingListsRealTime(currentUser: User) {
        viewModelScope.launch {
            shoppingListRepository.getCurrentUserShoppingListsRealTime(currentUser).collect { result ->
                when(result.status)
                {
                    Status.LOADING -> {
                        _shoppingLists.postValue(Resource.Loading(true))
                    }
                    Status.SUCCESS -> {
                        result._data?.map { _shoppingList ->
                            _shoppingList.friendsSharedWith.map { friendId ->
                                userRepository.getUserFromFirestore(friendId).collect { userResult ->
                                    when(userResult.status)
                                    {
                                        Status.LOADING -> {
                                            _shoppingLists.postValue(Resource.Loading(true))
                                        }
                                        Status.SUCCESS -> {
                                            userResult._data?.let {
                                                _shoppingList.usersSharedWith.add(it)
                                                _shoppingLists.postValue(Resource.Success(_shoppingList))
                                            }
                                        }
                                        Status.ERROR -> {
                                            result.message?.let {
                                                _shoppingLists.postValue(Resource.Error(it))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Status.ERROR -> {
                        result.message?.let {
                            _shoppingLists.postValue(Resource.Error(it))
                        }
                    }
                }
            }
        }
    }


    sealed class ShoppingListsFragmentsEvent {
        object NavigateToSplashFragment : ShoppingListsFragmentsEvent()
    }



}