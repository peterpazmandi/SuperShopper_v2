package com.inspirecoding.supershopper.ui.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.repository.datastore.DataStoreRepository
import com.inspirecoding.supershopper.ui.register.RegisterViewModel
import com.inspirecoding.supershopper.ui.shoppinglists.ShoppingListsViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsViewModel @ViewModelInject constructor(
    private val dataStoreRepository: DataStoreRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    // CONST
    private val TAG = this.javaClass.simpleName

    val currentUser = state.getLiveData<User>(ShoppingListsViewModel.ARG_KEY_USER)


    private val _settingsEvents = Channel<SettingsEvent>()
    val settingsEvents = _settingsEvents.receiveAsFlow()



    val notificationsSettingsFromDataStore = dataStoreRepository.readNotificationsSettingFromDataStore.asLiveData()
    fun saveNotificationsSettingsToDataStore(areTurnedOn: Boolean) = viewModelScope.launch {
        dataStoreRepository.saveNotificationsSettingToDataStore(areTurnedOn = areTurnedOn)
    }

    val nightModeSettingsFromDataStore = dataStoreRepository.readNightModeSettingFromDataStore.asLiveData()
    fun saveNightModeSettingsToDataStore(isTurnedOn: Boolean) = viewModelScope.launch {
        dataStoreRepository.saveNightModeSettingToDataStore(isTurnedOn = isTurnedOn)
        if(isTurnedOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }


    /** Events **/
    fun onCategoriesSelected() {
        viewModelScope.launch {
            _settingsEvents.send(SettingsEvent.NavigateToCategoriesFragment)
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
            currentUser.value?.let { _currentUser ->
                _settingsEvents.send(SettingsEvent.ShareTheAppClicked(_currentUser))
            }
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
        data class ShareTheAppClicked(val currentUser: User): SettingsEvent()
        object RateTheAppClicked: SettingsEvent()
        object NavigateToTermsAndConditionsFragment: SettingsEvent()
        object NavigateToPrivacyPolicyFragment: SettingsEvent()
        data class ShowErrorMessage(val message: String) : SettingsEvent()
    }


}