package com.agos.astore.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.agos.astore.R
import com.agos.astore.format
import com.agos.astore.model.Cart

class RCart(
) : ListAdapter<Cart, RCart.ViewHolder>(CartDiffCallback) {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val productTextView: TextView = itemView.findViewById(R.id.product)
        private val priceTextView: TextView = itemView.findViewById(R.id.price)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantity)
        private val totalTextView: TextView = itemView.findViewById(R.id.total)
        private var current: Cart? = null

        @SuppressLint("SimpleDateFormat")
        fun bind(item: Cart) {
            current = item
            productTextView.text = current?.productName
            priceTextView.text = current?.price!!.format()
            quantityTextView.text = current?.quantity.toString()
            totalTextView.text = current?.total!!.format()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

object CartDiffCallback : DiffUtil.ItemCallback<Cart>() {
    override fun areItemsTheSame(oldItem: Cart, newItem: Cart): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Cart, newItem: Cart): Boolean {
        return oldItem.productId == newItem.productId
    }
}