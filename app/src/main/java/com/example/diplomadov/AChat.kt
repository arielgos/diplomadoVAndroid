package com.example.diplomadov

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diplomadov.adapter.RMessage
import com.example.diplomadov.databinding.ActivityChatBinding
import com.example.diplomadov.model.Message
import com.example.diplomadov.model.User
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*

class AChat : AppCompatActivity() {

    private lateinit var user: User
    private lateinit var reference: DatabaseReference
    var messageList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.chat)

        user = intent.extras?.get("user") as User

        /**
         * Analytics
         */
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Chat")
        }

        /**
         * Realtime Database
         */

        reference = FirebaseDatabase.getInstance().getReference("chats").child(user.id)

        binding.message.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                binding.send.callOnClick()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.send.setOnClickListener {
            val key = reference.push().key
            val message = Message(
                uid = user.id,
                name = user.name,
                message = binding.message.text.toString(),
                date = Date().time
            )
            reference.child(key!!).setValue(message)
            binding.message.text = null
        }

        reference.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue<Message>()
                message?.own = message?.uid == user.id
                Log.d(Utils.tag, "${message?.message} / ${message?.own}")
                messageList.add(message!!)
                binding.recyclerView.adapter?.notifyDataSetChanged()
                binding.recyclerView.scrollToPosition(messageList.size - 1)
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

        val messageAdapter = RMessage(applicationContext)
        messageAdapter.submitList(messageList)
        binding.recyclerView.hasFixedSize()
        binding.recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        binding.recyclerView.adapter = messageAdapter

    }
}