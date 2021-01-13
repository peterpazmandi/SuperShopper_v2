package com.inspirecoding.supershopper.repository.datastore

import androidx.datastore.preferences.core.preferencesKey

object PreferenceKeys {
    val notificationsSetting = preferencesKey<Boolean>("notificationsSetting")
    val nightModeSetting = preferencesKey<Boolean>("nightModeSetting")
}