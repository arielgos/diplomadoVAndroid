package com.example.diplomadov

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.diplomadov.databinding.ActivityMainBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class AMain : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    val tag = "Firebase"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Main")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        }

        binding.button.setOnClickListener {
            throw RuntimeException("Test Crash")
        }

        /**
         * Remote Config
         */

        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 1
        }

        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(tag, "Config params updated: $updated")
                    Log.d(tag, "Welcome Message ${remoteConfig["welcomeMessage"].asString()}")
                    Log.d(tag, "Version ${remoteConfig["version"].asLong()}")

                    supportActionBar?.title = "${remoteConfig["welcomeMessage"].asString()} [${remoteConfig["version"].asLong()}]"
                }
            }
    }
}