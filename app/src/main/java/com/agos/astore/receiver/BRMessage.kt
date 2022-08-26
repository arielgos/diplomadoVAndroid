package com.agos.astore.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.agos.astore.Utils

class BRMessage : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.extras != null) {
            val bundle = intent.extras
            if (bundle != null) {
                for (key in bundle.keySet()) {
                    Log.d(Utils.tag, "Key: $key - Value: ${bundle.get(key)}")
                }
                Toast.makeText(context, "${bundle.get("body")}", Toast.LENGTH_LONG).show()
            }
        }
    }
}