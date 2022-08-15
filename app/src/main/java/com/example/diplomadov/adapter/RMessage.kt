package com.example.diplomadov.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.diplomadov.R
import com.example.diplomadov.Utils
import com.example.diplomadov.model.Message
import java.text.SimpleDateFormat

class RMessage(
    private val context: Context
) : ListAdapter<Message, RMessage.ViewHolder>(DiffCallback) {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val container: LinearLayout = itemView.findViewById(R.id.container)
        private val background: LinearLayout = itemView.findViewById(R.id.background)
        private val userTextView: TextView = itemView.findViewById(R.id.user)
        private val messageTextView: TextView = itemView.findViewById(R.id.message)
        private val dateTextView: TextView = itemView.findViewById(R.id.date)
        private var currentMessage: Message? = null

        @SuppressLint("SimpleDateFormat")
        fun bind(message: Message, context: Context) {
            currentMessage = message
            if (!currentMessage!!.own) {
                container.gravity = Gravity.START
                background.background = ContextCompat.getDrawable(context, R.drawable.curve_teal)
                background.gravity = Gravity.START
                userTextView.visibility = View.VISIBLE
            }
            userTextView.text = currentMessage?.name
            messageTextView.text = currentMessage?.message
            dateTextView.text = SimpleDateFormat(Utils.dateFormat).format(currentMessage?.date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message, context)
    }
}

object DiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.date == newItem.date
    }
}