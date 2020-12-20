package com.inspirecoding.supershopper.ui.settings

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inspirecoding.supershopper.ui.register.RegisterViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    private val _settingsEvents = Channel<SettingsEvent>()
    val settingsEvents = _settingsEvents.receiveAsFlow()






    /** Events **/
    fun onCategoriesSelected() {
        viewModelScope.launch {
            _settingsEvents.send(SettingsEvent.NavigateToCategoriesFragment)
        }
    }
    fun onNotificationsSelected() {
        viewModelScope.launch {
            _settingsEvents.send(SettingsEvent.NavigateToNotificationsFragment)
        }
    }
    fun onTermsAndConditionSelected() {
        viewModelScope.launch {
            _settingsEvents.send(SettingsEvent.NavigateToTermsAndConditionsFragment)
        }
    }
    fun onPrivacyPolicySelected() {
        viewModelScope.launch {
            _settingsEvents.send(SettingsEvent.NavigateToPrivacyPolicyFragment)
        }
    }
    fun onShareTheAppSelected() {
        viewModelScope.launch {
            _settingsEvents.send(SettingsEvent.ShareTheAppClicked)
        }
    }
    fun onRateTheAppSelected() {
        viewModelScope.launch {
            _settingsEvents.send(SettingsEvent.RateTheAppClicked)
        }
    }
    fun onShowErrorMessage(message: String) {
        viewModelScope.launch {
            _settingsEvents.send(SettingsEvent.ShowErrorMessage(message))
        }
    }


    sealed class SettingsEvent {
        object NavigateToCategoriesFragment: SettingsEvent()
        object NavigateToNotificationsFragment: SettingsEvent()
        object ShareTheAppClicked: SettingsEvent()
        object RateTheAppClicked: SettingsEvent()
        object NavigateToTermsAndConditionsFragment: SettingsEvent()
        object NavigateToPrivacyPolicyFragment: SettingsEvent()
        data class ShowErrorMessage(val message: String) : SettingsEvent()
    }


}