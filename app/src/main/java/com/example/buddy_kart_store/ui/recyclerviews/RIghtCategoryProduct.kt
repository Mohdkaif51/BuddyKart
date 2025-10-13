package com.example.buddy_kart_store.ui.recyclerviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.CategoryProduct
import com.example.buddy_kart_store.ui.Home.ProductDetailPage

class RIghtCategoryProduct(
    private val products: List<CategoryProduct>
) :
    RecyclerView.Adapter<RIghtCategoryProduct.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.productImage)
        val name: TextView = itemView.findViewById(R.id.productName)
        val price: TextView = itemView.findViewById(R.id.productPrice)
        val discount: TextView = itemView.findViewById(R.id.discount)
        val button = itemView.findViewById<TextView>(R.id.homeAddButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_product_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.name.text = product.name
        holder.price.text = "₹${product.price}"
        holder.discount.text = "₹${product.discount}"
        holder.discount.visibility = View.GONE


        holder.discount.paintFlags =
            holder.discount.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG


        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .placeholder(R.drawable.download)
            .error(R.drawable.download)
            .into(holder.image)




        holder.itemView.setOnClickListener {
            ProductDetailPage.launch(holder.itemView.context, product.productId.toString())

        }
    }

    override fun getItemCount(): Int = products.size
}
