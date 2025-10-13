package com.example.buddy_kart_store.ui.recyclerviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.OrderItem
import com.example.buddy_kart_store.ui.Home.ProductDetailPage

class OrderSummeryRecycler(private var items: List<OrderItem>) :
    RecyclerView.Adapter<OrderSummeryRecycler.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage = itemView.findViewById<ImageView>(R.id.productImage)
        val productName = itemView.findViewById<TextView>(R.id.productName)
        val productQuantity = itemView.findViewById<TextView>(R.id.productQuantity)
        val productPrice = itemView.findViewById<TextView>(R.id.productPrice)
        val progressBar = itemView.findViewById<View>(R.id.progressbar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.orderdetailcard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.productName.text = item.name
        holder.productQuantity.text = "Qty: ${item.quantity}"
        holder.productPrice.text = item.price

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_launcher_foreground) // optional placeholder
            .into(holder.productImage)

        holder.itemView.setOnClickListener {
            ProductDetailPage.launch(holder.itemView.context, item.id)

        }
    }

    override fun getItemCount(): Int = items.size
}
