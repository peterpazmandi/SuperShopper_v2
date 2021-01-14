package com.inspirecoding.supershopper.repository.datastore

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.createDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : DataStoreRepository {

    override val PREFERENCES_NAME = "myPreferences"


    override val dataStore: DataStore<Preferences> = appContext.createDataStore(
        name = PREFERENCES_NAME
    )



    override suspend fun saveNotificationsSettingToDataStore(areTurnedOn: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.notificationsSetting] = areTurnedOn
        }
    }
    override val readNotificationsSettingFromDataStore: Flow<Boolean?> =
        dataStore.data
        .catch { exception ->
            if(exception is IOException) {
                Log.e("DataStore", exception.message.toString())
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferenceKeys.notificationsSetting]
        }



    override suspend fun saveNightModeSettingToDataStore(isTurnedOn: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.nightModeSetting] = isTurnedOn
        }
    }
    override val readNightModeSettingFromDataStore: Flow<Boolean?> =
        dataStore.data
            .catch { exception ->
                if(exception is IOException) {
                    Log.e("DataStore", exception.message.toString())
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferenceKeys.nightModeSetting]
            }



}