package com.agos.astore.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.agos.astore.R
import com.agos.astore.format
import com.agos.astore.model.Order
import com.agos.astore.toFormattedString

class ROrder(
    private val context: Context
) : ListAdapter<Order, ROrder.ViewHolder>(OrderDiffCallback) {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var current: Order? = null
        private val statusTextView: TextView = itemView.findViewById(R.id.status)
        private val idTextView: TextView = itemView.findViewById(R.id.id)
        private val dateTextView: TextView = itemView.findViewById(R.id.date)
        private val detailsTextView: TextView = itemView.findViewById(R.id.details)
        private val totalTextView: TextView = itemView.findViewById(R.id.total)

        @SuppressLint("SimpleDateFormat")
        fun bind(item: Order, context: Context) {
            when (item.status) {
                0 -> {
                    statusTextView.setBackgroundColor(context.resources.getColor(R.color.purple_200))
                }
                1 -> {
                    statusTextView.setBackgroundColor(context.resources.getColor(R.color.purple_500))
                }
                2 -> {
                    statusTextView.setBackgroundColor(context.resources.getColor(R.color.purple_700))
                }
            }
            idTextView.text = item.id
            dateTextView.text = item.date.toFormattedString()
            detailsTextView.text = "Productos(${item.details.count()})"
            totalTextView.text = item.total.format()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, context)
    }
}

object OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.date == newItem.date
    }
}
