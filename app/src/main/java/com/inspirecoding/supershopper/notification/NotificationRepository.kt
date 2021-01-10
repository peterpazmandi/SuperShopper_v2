package com.inspirecoding.supershopper.notification

import kotlinx.coroutines.Job

interface NotificationRepository {

    fun postNotification(
        notification: PushNotification
    ): Job

}