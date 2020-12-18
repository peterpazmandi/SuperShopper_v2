package com.inspirecoding.supershopper.ui.login

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.user.UserRepository
import com.inspirecoding.supershopper.utils.ValidateMethods
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class LoginViewModel @ViewModelInject constructor(
    private val userRepository: UserRepository
): ViewModel() {

    private val _loginEventChannel = Channel<LoginEvent>()
    val loginEventChannel = _loginEventChannel.receiveAsFlow()

    val userResource = userRepository.userResource

    var email = ""
        set(value) {
            field = value
        }
    var password = ""
        set(value) {
            field = value
        }



    fun validateFields() : Boolean {
        var _errorMessage = ""

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

    fun loginUser() {
        viewModelScope.launch {
            userRepository.logInUserFromAuthWithEmailAndPassword(
                email, password
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
    fun onForgotPasswordSelected() {
        viewModelScope.launch {
            _loginEventChannel.send(LoginEvent.NavigateToForgotPasswordDialog)
        }
    }
    fun onRegistrationSelected() {
        viewModelScope.launch {
            _loginEventChannel.send(LoginEvent.NavigateToRegisterFragment)
        }
    }
    fun onSuccessfulLogin(user: User) {
        viewModelScope.launch {
            _loginEventChannel.send(LoginEvent.LoginCompletedEvent(user))
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _loginEventChannel.send(LoginEvent.ShowErrorMessage(message))
        }
    }

    sealed class LoginEvent {
        object NavigateToRegisterFragment : LoginEvent()
        object NavigateToForgotPasswordDialog : LoginEvent()
        data class ShowErrorMessage(val message: String) : LoginEvent()
        data class LoginCompletedEvent(val user: User) : LoginEvent()
    }






}