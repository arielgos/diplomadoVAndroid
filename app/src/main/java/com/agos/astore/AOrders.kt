package com.agos.astore

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.agos.astore.adapter.ROrder
import com.agos.astore.databinding.ActivityListBinding
import com.agos.astore.model.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AOrders : AppCompatActivity() {

    private lateinit var user: User
    var orders = mutableListOf<Order>()
    private lateinit var binding: ActivityListBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.orders)

        user = intent.extras?.get("user") as User

        /**
         * Firestore
         */
        FirebaseFirestore.getInstance()
            .collection("orders")
            .whereEqualTo("userId", user.id)
            .addSnapshotListener { snapshots, _ ->
                for (dc in snapshots!!.documentChanges) {
                    var order = dc.document.toObject(Order::class.java)
                    order.id = dc.document.id
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d(Utils.tag, "New order: ${order.id}")
                            orders.add(order)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d(Utils.tag, "Modified order: ${order.id}")
                            orders[orders.indexOfFirst {
                                it.id == order.id
                            }] = order
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d(Utils.tag, "Removed order: ${order.id}")
                            orders.removeAt(orders.indexOfFirst {
                                it.id == order.id
                            })
                        }
                    }
                }
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }

        val orderAdapter = ROrder(this@AOrders)
        orderAdapter.submitList(orders)
        binding.recyclerView.hasFixedSize()
        binding.recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        binding.recyclerView.adapter = orderAdapter

        /**
         * Analytics
         */
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Orders")
        }
    }
}