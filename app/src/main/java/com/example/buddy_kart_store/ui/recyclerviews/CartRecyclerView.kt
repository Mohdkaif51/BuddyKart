package com.example.buddy_kart_store.ui.recyclerviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.CartDetail

class CartRecyclerView(
    private val cartList: MutableList<CartDetail>,
    private val listener: CartItemListener
) : RecyclerView.Adapter<CartRecyclerView.ViewHolder>() {

    interface CartItemListener {
        fun onQuantityChanged(cartItem: CartDetail, newQuantity: Int)
        fun onItemDeleted(cartItem: CartDetail)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.imageView2)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.price)
        val productQuantity: TextView = itemView.findViewById(R.id.qtyText)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val decreaseQty: ImageView = itemView.findViewById(R.id.decreaseQty)
        val increaseQty: ImageView = itemView.findViewById(R.id.increaseQty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = cartList[position]
        holder.productName.text = product.name
        holder.productPrice.text = "₹${product.price}"
        holder.productQuantity.text = product.quantity.toString()

        // Load image
        Glide.with(holder.itemView.context)
            .load(product.image)
            .into(holder.productImage)

        // Decrease quantity
        holder.decreaseQty.setOnClickListener {
            if (product.quantity > 1) {
                product.quantity--
                holder.productQuantity.text = product.quantity.toString()
                listener.onQuantityChanged(product, product.quantity)
            }
        }

        // Increase quantity
        holder.increaseQty.setOnClickListener {
            product.quantity++
            holder.productQuantity.text = product.quantity.toString()
            listener.onQuantityChanged(product, product.quantity)
        }

        // Delete item
        holder.deleteButton.setOnClickListener {
            listener.onItemDeleted(product)
            cartList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, cartList.size)
        }
    }

    override fun getItemCount(): Int = cartList.size
}
