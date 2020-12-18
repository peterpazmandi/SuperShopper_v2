package com.inspirecoding.supershopper.ui.register

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.utils.ValidateMethods
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegisterViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    private val TAG = this.javaClass.simpleName
    private val USERNAME = "username"

    private val _registrationEventChannel = Channel<RegistrationEvent>()
    val registrationEventChannel = _registrationEventChannel.receiveAsFlow()

    val userResource = userRepository.userResource

    var username = state.get<String>(USERNAME) ?: ""
        set(value) {
            field = value
            state[USERNAME] = value
        }

    var email = ""

    var password = ""

    fun areTheFieldsValid(): Boolean {
        var _errorMessage = ""

        _errorMessage += if (_errorMessage != "") {
            "\n${ValidateMethods.validateUsername(username)}"
        } else {
            ValidateMethods.validateUsername(username)
        }
        _errorMessage += if (_errorMessage != "") {
            "\n${ValidateMethods.validateEmail(email)}"
        } else {
            ValidateMethods.validateEmail(email)
        }
        _errorMessage += if (_errorMessage != "") {
            "\n${ValidateMethods.validatePassword(password)}"
        } else {
            ValidateMethods.validatePassword(password)
        }

        if (_errorMessage != "") onShowErrorMessage(_errorMessage)

        return _errorMessage.isEmpty()
    }

    fun registerUser() {
        viewModelScope.launch {
            userRepository.registerUserFromAuthWithEmailAndPassword(
                username, email, password
            )
        }
    }

    /** Facebook **/
    fun signInWithFacebook(fragment: Fragment) {
        viewModelScope.launch {
            userRepository.signInWithFacebook(fragment)
        }
    }

    /** Google **/
    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            userRepository.signInWithGoogle(activity)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        userRepository.onActivityResult(requestCode, resultCode, data, viewModelScope)
    }


    /** Events **/
    fun onTermsAndConditionSelected() {
        viewModelScope.launch {
            _registrationEventChannel.send(RegistrationEvent.NavigateToTermsAndConditionFragment)
        }
    }

    fun onPrivacyPolicySelected() {
        viewModelScope.launch {
            _registrationEventChannel.send(RegistrationEvent.NavigateToPrivacyPolicyFragment)
        }
    }

    fun onLoginSelected() {
        viewModelScope.launch {
            _registrationEventChannel.send(RegistrationEvent.NavigateToLoginFragment)
        }
    }

    fun onSuccessfulRegistration(user: User) {
        viewModelScope.launch {
            _registrationEventChannel.send(RegistrationEvent.RegistrationCompletedEvent(user))
        }
    }

    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _registrationEventChannel.send(RegistrationEvent.ShowErrorMessage(message))
        }
    }


    sealed class RegistrationEvent {
        object NavigateToLoginFragment : RegistrationEvent()
        object NavigateToTermsAndConditionFragment : RegistrationEvent()
        object NavigateToPrivacyPolicyFragment : RegistrationEvent()
        data class ShowErrorMessage(val message: String) : RegistrationEvent()
        data class RegistrationCompletedEvent(val user: User) : RegistrationEvent()
    }


}