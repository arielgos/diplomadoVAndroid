package com.agos.astore

import android.annotation.SuppressLint
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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.agos.astore.adapter.OnProductClickListener
import com.agos.astore.adapter.RProduct
import com.agos.astore.databinding.ActivityMainBinding
import com.agos.astore.model.Cart
import com.agos.astore.model.Product
import com.agos.astore.model.User
import com.agos.astore.service.SMessaging
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.FileInputStream
import java.util.*

class AMain : AppCompatActivity() {

    private var currentUser: User? = null
    private var shoppingReference: DatabaseReference? = null
    private var cartBadge: TextView? = null
    private var products = mutableListOf<Product>()

    @SuppressLint("NotifyDataSetChanged")
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


                /**
                 * Realtime Database
                 */
                shoppingReference = FirebaseDatabase.getInstance().getReference("carts").child(currentUser?.id!!)

                shoppingReference!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cartBadge?.text = snapshot.childrenCount.toString()
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

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
         * Image Actions
         */
        binding.imageSearch.setOnClickListener {
            val intent = com.canhub.cropper.CropImage.activity()
                .setAspectRatio(Utils.imageWith, Utils.imageHeight)
                .getIntent(applicationContext)
            startActivityForResult(intent, Utils.requestSearchImage)
        }

        /**
         * Add Product
         */
        binding.addProduct.setOnClickListener {

        }

        /**
         * Firestore
         */
        FirebaseFirestore.getInstance()
            .collection("products")
            .whereGreaterThan("price", 0.0)
            .whereEqualTo("status", true)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(Utils.tag, "${document.id} => ${document.data}")
                    products.add(document.toObject(Product::class.java))
                }
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }.addOnFailureListener {
                it.printStackTrace()
            }


        /**
         * Actions
         */
        val productAdapter = RProduct(applicationContext, OnProductClickListener { item ->

            shoppingReference?.child(item.id)?.get()?.addOnSuccessListener {
                var cart = it.getValue<Cart>()
                if (cart == null) {
                    cart = Cart(
                        productId = item.id,
                        productName = item.name,
                        price = item.price,
                        quantity = 0,
                        total = item.price
                    )
                }
                cart.quantity = cart.quantity?.plus(1)
                cart.total = cart.price?.times(cart.quantity!!)

                shoppingReference?.child(item.id)?.setValue(cart)
            }

            Toast.makeText(applicationContext, "${item.name} agregado al carrito", Toast.LENGTH_SHORT).show()
        })

        productAdapter.submitList(products)
        binding.recyclerView.hasFixedSize()
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.adapter = productAdapter

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
        cartBadge = menu.findItem(R.id.action_cart).actionView.findViewById(R.id.cart_badge)
        menu.findItem(R.id.action_cart).actionView.setOnClickListener {
            val intent = Intent(this@AMain, ACart::class.java)
            intent.putExtra("user", currentUser)
            startActivity(intent)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_chat -> {
                val intent = Intent(this@AMain, AChat::class.java)
                intent.putExtra("user", currentUser)
                startActivity(intent)
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
                Utils.requestSearchImage -> {
                    val result = com.canhub.cropper.CropImage.getActivityResult(data)
                    Glide.with(this)
                        .asBitmap()
                        .load(result!!.uri)
                        .into(target)
                    /**
                     * Image Labeling
                     */
                    val image = InputImage.fromFilePath(applicationContext, result!!.uri)
                    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                    labeler.process(image)
                        .addOnSuccessListener { labels ->
                            for (label in labels) {
                                val text = label.text
                                val confidence = label.confidence
                                val index = label.index
                            }
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
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