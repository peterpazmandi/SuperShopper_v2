package com.inspirecoding.supershopper.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.inspirecoding.supershopper.repository.datastore.DataStoreRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseService : FirebaseMessagingService() {

    // CONST
    private val TAG = this.javaClass.simpleName

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val messageTitle = message.data["title"]
        val messageText = message.data["message"]

        if (!messageTitle.isNullOrEmpty() && !messageText.isNullOrEmpty()) {
            sendNotification(messageTitle, messageText)
        }
    }

    private fun sendNotification(title: String, message: String) = CoroutineScope(Dispatchers.IO).launch {
        dataStoreRepository.readNotificationsSettingFromDataStore.collect { areTurnedOn ->
            if(areTurnedOn != null && areTurnedOn) {
                NotificationBuilder.createNotification(
                    title = title,
                    message = message,
                    context = this@FirebaseService,
                )
            } else {
                /**
                * Flow will deliver the result when there will be an active observer
                * But, if we cancel the CoroutineScope, than the message won't be delivered
                **/
                cancel()
            }
        }
    }
}