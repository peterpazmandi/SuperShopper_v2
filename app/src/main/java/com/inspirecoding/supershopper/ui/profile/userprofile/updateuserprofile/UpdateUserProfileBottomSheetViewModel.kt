package com.inspirecoding.supershopper.ui.profile.userprofile.updateuserprofile

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firestore.v1.UpdateDocumentRequest
import com.inspirecoding.supershopper.ui.profile.currentuserprofile.CurrentUserProfileViewModel
import com.inspirecoding.supershopper.ui.profile.currentuserprofile.CurrentUserProfileViewModel.Companion.FIELD_TO_CHANGE
import com.inspirecoding.supershopper.ui.register.RegisterViewModel
import com.inspirecoding.supershopper.utils.ValidateMethods
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class UpdateUserProfileBottomSheetViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    val fieldToChange = state.getLiveData<String>(FIELD_TO_CHANGE)

    private val _fragmentEvent = Channel<FragmentEvent>()
    val fragmentEvent = _fragmentEvent.receiveAsFlow()

    var newValue = ""

    private fun validateField(): Boolean {
        var _errorMessage = ""
        fieldToChange.value?.let { _fieldToChange ->
            when(_fieldToChange)
            {
                CurrentUserProfileViewModel.USERNAME -> {
                    _errorMessage = ValidateMethods.validateUsername(newValue)
                }
                CurrentUserProfileViewModel.EMAIL -> {
                    _errorMessage = ValidateMethods.validateEmail(newValue)

                }
                CurrentUserProfileViewModel.PASSWORD -> {
                    _errorMessage = ValidateMethods.validatePassword(newValue)
                }
            }
        }

        if (_errorMessage != "") onShowErrorMessage(_errorMessage)

        return _errorMessage.isEmpty()
    }

    fun onChangeSelected() {
        viewModelScope.launch {
            fieldToChange.value?.let { _fieldToChange ->
                if(validateField()) {
                    when(_fieldToChange)
                    {
                        CurrentUserProfileViewModel.USERNAME -> {
                            _fragmentEvent.send(FragmentEvent.UpdateUserName(newValue))
                        }
                        CurrentUserProfileViewModel.EMAIL -> {
                            _fragmentEvent.send(FragmentEvent.UpdateEmailAddress(newValue))
                        }
                        CurrentUserProfileViewModel.PASSWORD -> {
                            _fragmentEvent.send(FragmentEvent.UpdatePassword(newValue))
                        }
                    }
                }
            }
        }
    }

    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _fragmentEvent.send(FragmentEvent.ShowErrorMessage(message))
        }
    }







    sealed class FragmentEvent {
        data class UpdateUserName(val fieldToChange: String): FragmentEvent()
        data class UpdateEmailAddress(val fieldToChange: String): FragmentEvent()
        data class UpdatePassword(val fieldToChange: String): FragmentEvent()
        data class ShowErrorMessage(val message: String): FragmentEvent()
    }
}