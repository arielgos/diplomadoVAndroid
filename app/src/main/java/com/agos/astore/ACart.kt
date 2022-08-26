package com.agos.astore

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.agos.astore.adapter.RCart
import com.agos.astore.databinding.ActivityCartBinding
import com.agos.astore.model.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*

class ACart : AppCompatActivity() {

    private lateinit var user: User
    private lateinit var shoppingReference: DatabaseReference
    var shoppingList = mutableListOf<Cart>()
    private lateinit var binding: ActivityCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.cart)

        user = intent.extras?.get("user") as User

        /**
         * Analytics
         */
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Cart")
        }

        /**
         * Realtime Database
         */

        shoppingReference = FirebaseDatabase.getInstance().getReference("carts").child(user.id)


        shoppingReference.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val cart = snapshot.getValue<Cart>()
                shoppingList.add(cart!!)
                binding.recyclerView.adapter?.notifyDataSetChanged()
                binding.recyclerView.scrollToPosition(shoppingList.size - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }

        })

        val cartAdapter = RCart(this@ACart)
        cartAdapter.submitList(shoppingList)
        binding.recyclerView.hasFixedSize()
        binding.recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        binding.recyclerView.adapter = cartAdapter

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cart, menu)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clean -> {
                clean()
            }
            R.id.action_save -> {

                var order = Order()
                order.userId = user.id
                order.date = Date()
                order.total = 0.0
                order.status = 0
                order.details = mutableListOf()
                shoppingList.forEach {
                    var detail = Detail()
                    detail.productId = it.productId
                    detail.productName = it.productName
                    detail.productImage = it.productImage
                    detail.price = it.price!!
                    detail.quantity = it.quantity
                    detail.total = it.total
                    order.details.add(detail)
                    order.total += it.total!!
                }

                FirebaseFirestore.getInstance()
                    .collection("orders")
                    .add(order)
                    .addOnCompleteListener {
                        clean()
                        finish()
                    }.addOnFailureListener {
                        it.printStackTrace()
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun clean() {
        shoppingReference.removeValue()
        shoppingList.clear()
        binding.recyclerView.adapter?.notifyDataSetChanged()
        binding.recyclerView.scrollToPosition(shoppingList.size - 1)
    }
}