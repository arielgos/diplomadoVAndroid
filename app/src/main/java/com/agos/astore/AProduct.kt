package com.agos.astore

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.agos.astore.databinding.ActivityProductBinding
import com.agos.astore.model.Product
import com.agos.astore.model.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.FileInputStream
import java.util.*


class AProduct : AppCompatActivity() {

    private lateinit var user: User
    private lateinit var product: Product
    private lateinit var binding: ActivityProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.product)

        user = intent.extras?.get("user") as User
        product = intent.extras?.get("product") as Product

        with(binding) {
            name.setText(product.name)
            description.setText(product.description)
            tags.setText(product.tags)
            price.setText(product.price.format())
            status.isChecked = product.status

            if (product.id.isEmpty()) {
                status.isChecked = false
                status.visibility = View.GONE
            }

            save.setOnClickListener {

                product.name = name.text.toString()
                product.image = ""
                product.description = description.text.toString()
                product.tags = tags.text.toString()
                product.price = price.text.toString().toDouble()
                product.status = status.isChecked

                val collection = FirebaseFirestore.getInstance()
                    .collection("products")
                if (product.id == "") {
                    val newDocument = collection.document()
                    product.id = newDocument.id
                    newDocument.set(product)
                        .addOnSuccessListener {
                            setResult(RESULT_OK, Intent())
                            finish()
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                } else {
                    collection.document(product.id)
                        .set(product)
                        .addOnSuccessListener {
                            setResult(RESULT_OK, Intent())
                            finish()
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                }


            }

            cancel.setOnClickListener {
                onBackPressed()
            }

        }

        /**
         * Image Actions
         */
        binding.image.setOnClickListener {
            val intent = com.canhub.cropper.CropImage.activity()
                .setAspectRatio(Utils.imageWith, Utils.imageHeight)
                .getIntent(applicationContext)
            startActivityForResult(intent, Utils.requestSearchImage)
        }
        /**
         * Analytics
         */
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Product")
        }

    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED, Intent())
        finish()
    }

    @SuppressLint("NotifyDataSetChanged")
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

                    Glide.with(this)
                        .asBitmap()
                        .load(result!!.uri)
                        .into(binding.image)
                    /**
                     * Image Labeling
                     */
                    val image = InputImage.fromFilePath(applicationContext, result!!.uri)
                    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                    labeler.process(image)
                        .addOnSuccessListener { labels ->
                            var tags = mutableListOf<String>()
                            for (label in labels) {
                                val text = label.text
                                val confidence = label.confidence
                                val index = label.index
                                Log.d(Utils.tag, "Label [$index] $confidence / $text")
                                tags.add(text)
                            }
                            product.tags = tags.joinToString { "," }
                            binding.tags.setText(product.tags)
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