package com.inspirecoding.supershopper.repository.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow


interface DataStoreRepository {
    val PREFERENCES_NAME: String

    val dataStore: DataStore<Preferences>
    val readNotificationsSettingFromDataStore: Flow<Boolean>
    val readNightModeSettingFromDataStore: Flow<Boolean>

    suspend fun saveNotificationsSettingToDataStore(areTurnedOn: Boolean)
    suspend fun saveNightModeSettingToDataStore(isTurnedOn: Boolean)
}