package com.agos.astore.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.agos.astore.R
import com.agos.astore.Utils
import com.agos.astore.format
import com.agos.astore.model.Cart
import com.bumptech.glide.Glide

class RCart(
    private val context: Context
) : ListAdapter<Cart, RCart.ViewHolder>(CartDiffCallback) {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.image)
        private val productTextView: TextView = itemView.findViewById(R.id.product)
        private val priceTextView: TextView = itemView.findViewById(R.id.price)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantity)
        private val totalTextView: TextView = itemView.findViewById(R.id.total)
        private var current: Cart? = null

        @SuppressLint("SimpleDateFormat")
        fun bind(item: Cart, context: Context) {
            current = item

            if (current?.productImage?.isNotEmpty() == true) {
                Glide.with(context)
                    .asBitmap()
                    .load("${Utils.imageUrl}thumb_${current?.productImage}?alt=media")
                    .into(imageView)
            }

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
        holder.bind(item, context)
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