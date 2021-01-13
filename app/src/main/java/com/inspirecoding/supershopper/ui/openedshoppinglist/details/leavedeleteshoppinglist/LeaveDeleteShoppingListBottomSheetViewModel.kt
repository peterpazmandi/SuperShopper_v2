package com.inspirecoding.supershopper.ui.openedshoppinglist.details.leavedeleteshoppinglist

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.ui.openedshoppinglist.OpenedShoppingListViewModel
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import com.inspirecoding.supershopper.utils.extensions.combineWith
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LeaveDeleteShoppingListBottomSheetViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val _leaveDeleteEventChannel = Channel<LeaveDeleteShoppingListEvent>()
    val leaveDeleteEventChannel = _leaveDeleteEventChannel.receiveAsFlow()

    val openedShoppingList = state.getLiveData<ShoppingList>(OpenedShoppingListViewModel.ARG_KEY_OPENEDSHOPPINGLIST)
    val currentUser = state.getLiveData<User>(ShoppingListsViewModel.ARG_KEY_USER)

    val shoppingListWithCurrentUser = openedShoppingList.combineWith(currentUser) { shoppingList, currentUser ->
        Pair(shoppingList, currentUser)
    }


    /** Events **/
    fun onNavigateBackWithResult() {
        viewModelScope.launch {
            shoppingListWithCurrentUser.value?.let { pair ->
                _leaveDeleteEventChannel.send(LeaveDeleteShoppingListEvent.NavigateBackWithResult(
                    pair.first?.usersSharedWith, pair.second
                )
                )
            }
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _leaveDeleteEventChannel.send(LeaveDeleteShoppingListEvent.ShowErrorMessage(message))
        }
    }


    sealed class LeaveDeleteShoppingListEvent {
        data class NavigateBackWithResult(val listOfMembers: MutableList<User>?, val currentUserId: User?): LeaveDeleteShoppingListEvent()
        data class ShowErrorMessage(val message: String) : LeaveDeleteShoppingListEvent()
    }

}