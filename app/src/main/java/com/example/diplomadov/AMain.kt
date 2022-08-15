package com.example.diplomadov

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.diplomadov.databinding.ActivityMainBinding
import com.example.diplomadov.model.User
import com.example.diplomadov.service.SMessaging
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.util.*

class AMain : AppCompatActivity() {

    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Remote Config
         */
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 10
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    supportActionBar?.title = "${remoteConfig["appTitle"].asString()} [${remoteConfig["version"].asLong()}]"
                }
            }

        /**
         * Firestore
         */
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)

                /**
                 * Cloud Messaging
                 */
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    val token = task.result
                    currentUser?.token = token
                    it.reference.set(currentUser!!)
                    Log.d(Utils.tag, "Push Notification token $token")
                })

            }.addOnFailureListener {
                it.printStackTrace()
            }


        /**
         * Cloud Messaging
         */
        FirebaseMessaging.getInstance().subscribeToTopic("amazingstore")
        FirebaseMessaging.getInstance().subscribeToTopic("android")

        /**
         * Analytics
         */
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Main")
        }

        /**
         * Actions
         */
        binding.camera.setOnClickListener {
            val intent = com.canhub.cropper.CropImage.activity()
                .setAspectRatio(Utils.imageWith, Utils.imageHeight)
                .getIntent(applicationContext)
            startActivityForResult(intent, Utils.requestImage)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.extras != null) {
            val bundle = intent.extras
            if (bundle != null) {
                for (key in bundle.keySet()) {
                    Log.d(Utils.tag, "Main Activity - Key: $key - Value: ${bundle.get(key)}")
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(messageReceiver, IntentFilter(SMessaging.broadcastReceiver))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(messageReceiver)
        super.onPause()
    }

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.extras != null) {
                val bundle = intent.extras
                if (bundle != null) {
                    for (key in bundle.keySet()) {
                        Log.d(Utils.tag, "Key: $key - Value: ${bundle.get(key)}")
                    }

                    Toast.makeText(applicationContext, "${bundle.get("body")}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_chat -> {
                val intent = Intent(this@AMain, AChat::class.java)
                intent.putExtra("user", currentUser)
                startActivity(intent)
            }
            R.id.action_cart -> {

            }
            R.id.action_exit -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@AMain, ASplash::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Utils.requestImage -> {
                    val result = com.canhub.cropper.CropImage.getActivityResult(data)
                    Glide.with(this)
                        .asBitmap()
                        .load(result!!.uri)
                        .into(target)
                }
            }
        }
    }

    private val target = object : CustomTarget<Bitmap>() {

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            val fileName = "${UUID.randomUUID()}.jpg"
            val path = "${externalCacheDir?.absolutePath}/$fileName"
            val file = resource.createFile(path)

            FirebaseStorage.getInstance().reference
                .child(fileName)
                .putStream(FileInputStream(file))
                .addOnSuccessListener {
                    Log.d(Utils.tag, "Image ${it.task.result}")
                }.addOnFailureListener {
                    it.printStackTrace()
                }.addOnPausedListener {
                    Log.d(Utils.tag, "Upload is paused")
                }.addOnProgressListener {
                    val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
                    Log.d(Utils.tag, "Upload is $progress% done")
                }

        }

        override fun onLoadCleared(placeholder: Drawable?) {}
    }
}