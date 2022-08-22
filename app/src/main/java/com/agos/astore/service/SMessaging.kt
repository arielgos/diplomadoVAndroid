package com.agos.astore.service

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.agos.astore.Utils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SMessaging : FirebaseMessagingService() {

    companion object {
        const val broadcastReceiver = "broadcast"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val intent = Intent(broadcastReceiver)
        intent.putExtra("title", remoteMessage.notification?.title)
        intent.putExtra("body", remoteMessage.notification?.body)

        if (remoteMessage.data.isNotEmpty()) {
            for (key in remoteMessage.data.keys) {
                Log.d(Utils.tag, "Key: $key - Value: ${remoteMessage.data.get(key)}")
            }
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onNewToken(token: String) {
        Log.d(Utils.tag, "New Token $token")
    }
}