package com.inspirecoding.supershopper.repository.datastore

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Flow
import javax.inject.Inject

//class DataStoreRepositoryImpl @Inject constructor(
//    @ApplicationContext private val application: Application
//) {
//
//    private val PREFERENCES_NAME = "myPreferences"
//
//    private val firebaseInstanceToken = preferencesKey<String>("firebaseInstanceToken")
//
//    private val dataStore: DataStore<Preferences> = application.createDataStore(
//        name = PREFERENCES_NAME
//    )
//
//    suspend fun saveFirebaseInstanceTokenToDataStore(token: String) {
//        dataStore.edit { preferences ->
//            preferences[firebaseInstanceToken] = token
//        }
//    }
//
//    val readFromDataStore: Flow<String> =
//
//}