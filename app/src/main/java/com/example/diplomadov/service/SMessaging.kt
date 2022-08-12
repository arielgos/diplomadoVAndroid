package com.example.diplomadov.service

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.diplomadov.R
import com.example.diplomadov.Utils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SMessaging : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val intent = Intent(getString(R.string.default_channel))
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