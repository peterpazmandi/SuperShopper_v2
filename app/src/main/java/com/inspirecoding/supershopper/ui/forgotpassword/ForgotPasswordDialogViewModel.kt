package com.inspirecoding.supershopper.ui.forgotpassword

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.inspirecoding.supershopper.repository.auth.AuthRepository
import com.inspirecoding.supershopper.ui.register.RegisterViewModel
import com.inspirecoding.supershopper.utils.ValidateMethods
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.annotation.Resource

class ForgotPasswordDialogViewModel @ViewModelInject constructor(
    private val authRepository: AuthRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    private val _fragmentEvents = Channel<FragmentEvent>()
    val fragmentEvent = _fragmentEvents.receiveAsFlow()

    var email = ""


    fun areTheFieldsValid(): Boolean {
        var _errorMessage = ""

        _errorMessage += if (_errorMessage != "") {
            "\n${ValidateMethods.validateEmail(email)}"
        } else {
            ValidateMethods.validateEmail(email)
        }

        if (_errorMessage != "") onShowErrorMessage(_errorMessage)

        return _errorMessage.isEmpty()
    }


    fun resetEmail() {
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email).collect { _result ->
                when(_result)
                {
                    is com.inspirecoding.supershopper.data.Resource.Success -> {
                        onSuccessfulReset()
                    }
                    is com.inspirecoding.supershopper.data.Resource.Loading -> {
                        onRequestPending()
                    }
                    is com.inspirecoding.supershopper.data.Resource.Error -> {
                        _result.message?.let { _message ->
                            onShowErrorMessage(_message)
                        }
                    }
                }
            }
        }
    }
    fun onSuccessfulReset() {
        viewModelScope.launch {
            _fragmentEvents.send(FragmentEvent.SuccessfulReset)
        }
    }
    fun onRequestPending() {
        viewModelScope.launch {
            _fragmentEvents.send(FragmentEvent.RequestPending)
        }
    }
    fun onDismissDialog() {
        viewModelScope.launch {
            _fragmentEvents.send(FragmentEvent.DismissDialog)
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _fragmentEvents.send(FragmentEvent.ShowErrorMessage(message))
        }
    }





    sealed class FragmentEvent {
        object DismissDialog : FragmentEvent()
        object RequestPending : FragmentEvent()
        object SuccessfulReset : FragmentEvent()
        data class ShowErrorMessage(val message: String) : FragmentEvent()
    }

}