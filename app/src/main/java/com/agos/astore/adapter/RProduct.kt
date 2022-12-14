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
import com.bumptech.glide.Glide
import com.agos.astore.R
import com.agos.astore.Utils
import com.agos.astore.format
import com.agos.astore.model.Product

class RProduct(
    private val context: Context,
    private val onClickListener: OnProductClickListener,
    private val onLonClickListener: OnProductLongClickListener
) : ListAdapter<Product, RProduct.ViewHolder>(ProductDiffCallback) {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.image)
        private val nameTextView: TextView = itemView.findViewById(R.id.name)
        private val tagsTextView: TextView = itemView.findViewById(R.id.tags)
        private val priceTextView: TextView = itemView.findViewById(R.id.price)
        private var current: Product? = null

        @SuppressLint("SimpleDateFormat", "CheckResult")
        fun bind(item: Product, context: Context) {
            current = item
            if (current?.image?.isNotEmpty() == true) {
                Glide.with(context)
                    .asBitmap()
                    .load("${Utils.imageUrl}${current?.image}?alt=media")
                    .into(imageView)
            }
            nameTextView.text = current?.name
            tagsTextView.text = current?.tags
            priceTextView.text = current?.price!!.format()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(item)
        }

        holder.itemView.setOnLongClickListener {
            onLonClickListener.onLongClick(item)
            return@setOnLongClickListener true
        }

        holder.bind(item, context)

    }
}

object ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }
}

class OnProductClickListener(val clickListener: (item: Product) -> Unit) {
    fun onClick(item: Product) = clickListener(item)
}

class OnProductLongClickListener(val longClickListener: (item: Product) -> Unit) {
    fun onLongClick(item: Product) = longClickListener(item)
}