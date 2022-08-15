package com.example.diplomadov

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplomadov.adapter.RCart
import com.example.diplomadov.adapter.RMessage
import com.example.diplomadov.databinding.ActivityCartBinding
import com.example.diplomadov.databinding.ActivityChatBinding
import com.example.diplomadov.model.Cart
import com.example.diplomadov.model.Message
import com.example.diplomadov.model.User
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*

class ACart : AppCompatActivity() {

    private lateinit var user: User
    private lateinit var shoppingReference: DatabaseReference
    var shoppingList = mutableListOf<Cart>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCartBinding.inflate(layoutInflater)
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

        val cartAdapter = RCart()
        cartAdapter.submitList(shoppingList)
        binding.recyclerView.hasFixedSize()
        binding.recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        binding.recyclerView.adapter = cartAdapter

    }
}