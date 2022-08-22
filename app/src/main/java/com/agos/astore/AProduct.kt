package com.agos.astore

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.agos.astore.databinding.ActivityProductBinding
import com.agos.astore.model.Product
import com.agos.astore.model.User
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class AProduct : AppCompatActivity() {

    private lateinit var user: User
    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProductBinding.inflate(layoutInflater)
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
}