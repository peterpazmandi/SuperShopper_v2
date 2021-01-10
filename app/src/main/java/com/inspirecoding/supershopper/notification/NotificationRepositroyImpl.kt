package com.inspirecoding.supershopper.notification

import com.inspirecoding.supershopper.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi,
    @ApplicationScope private val applicationScope: CoroutineScope
) : NotificationRepository {

    override fun postNotification(
        notification: PushNotification
    ) = applicationScope.launch {

        val response = notificationApi.postNotification(notification)
        if (!response.isSuccessful) {
            println("Response: ${response.errorBody()}")
        }

    }

}