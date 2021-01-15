package com.inspirecoding.supershopper

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.inspirecoding.supershopper.repository.datastore.DataStoreRepository
import kotlinx.coroutines.launch

class MainActivityViewModel@ViewModelInject constructor(
    private val dataStoreRepository: DataStoreRepository,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

    val notificationsSettingsFromDataStore = dataStoreRepository.readNotificationsSettingFromDataStore.asLiveData()
    val nightModeSettingsFromDataStore = dataStoreRepository.readNightModeSettingFromDataStore.asLiveData()


    fun saveNotificationsSettingsToDataStore(areTurnedOn: Boolean) = viewModelScope.launch {
        dataStoreRepository.saveNotificationsSettingToDataStore(areTurnedOn = areTurnedOn)
    }
    fun saveNightModeSettingsToDataStore(isTurnedOn: Boolean) = viewModelScope.launch {
        dataStoreRepository.saveNightModeSettingToDataStore(isTurnedOn = isTurnedOn)
    }


}