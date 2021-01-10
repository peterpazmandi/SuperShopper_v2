package com.inspirecoding.supershopper.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val messageTitle = message.data["title"]
        val messageText = message.data["message"]

        if(!messageTitle.isNullOrEmpty() && !messageText.isNullOrEmpty()) {
            NotificationBuilder.createNotification(
                title = messageTitle,
                message = messageText,
                context = this,
            )
        }
    }

}