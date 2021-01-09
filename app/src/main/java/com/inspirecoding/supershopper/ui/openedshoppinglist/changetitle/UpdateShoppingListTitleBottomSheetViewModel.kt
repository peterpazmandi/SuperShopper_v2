package com.inspirecoding.supershopper.ui.openedshoppinglist.changetitle

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspirecoding.supershopper.ui.profile.currentuserprofile.CurrentUserProfileViewModel
import com.inspirecoding.supershopper.utils.ValidateMethods
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class UpdateShoppingListTitleBottomSheetViewModel  @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    companion object {
        val TITLE = "title"
    }

    val previousTitle = state.getLiveData<String>(TITLE)

    private val _fragmentEvent = Channel<FragmentEvent>()
    val fragmentEvent = _fragmentEvent.receiveAsFlow()

    var newTitle = ""
    private fun validateField(): Boolean {
        val _errorMessage = ValidateMethods.validateUsername(newTitle)

        if (_errorMessage != "") onShowErrorMessage(_errorMessage)

        return _errorMessage.isEmpty()
    }

    fun onChangeSelected() {
        viewModelScope.launch {
            if(validateField()) {
                _fragmentEvent.send(FragmentEvent.UpdateTitle(newTitle))
            }
        }
    }

    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _fragmentEvent.send(FragmentEvent.ShowErrorMessage(message))
        }
    }







    sealed class FragmentEvent {
        data class UpdateTitle(val title: String): FragmentEvent()
        data class ShowErrorMessage(val message: String): FragmentEvent()
    }

}