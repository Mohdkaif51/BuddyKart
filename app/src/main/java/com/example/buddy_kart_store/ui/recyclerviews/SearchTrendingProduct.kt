package com.example.buddy_kart_store.ui.recyclerviews

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.SearchProduct
import com.example.buddy_kart_store.ui.Home.ProductDetailPage
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.utlis.SessionManager

class SearchTrendingProduct(
    private val products: MutableList<SearchProduct>,
    private val viewModel: WishListVM   // Pass ViewModel from Activity/Fragment
) : RecyclerView.Adapter<SearchTrendingProduct.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wishlist: ImageView = itemView.findViewById(R.id.btnWishlist)
        val image: ImageView = itemView.findViewById(R.id.productImage)
        val name: TextView = itemView.findViewById(R.id.productName)
        val price: TextView = itemView.findViewById(R.id.productPrice)
        val ratingContainer: LinearLayout = itemView.findViewById(R.id.ratingContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trending_product, parent, false)
        return ProductViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        // Bind product data
        holder.name.text = product.name
        holder.price.text = "₹${product.price}"

        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .placeholder(R.drawable.download)
            .error(R.drawable.download)
            .into(holder.image)

        // Set initial wishlist icon state
        if (product.favorite) {
            holder.wishlist.setImageResource(R.drawable.addedwishlist)

            holder.wishlist.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, R.color.red),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            holder.wishlist.setImageResource(R.drawable.fav)
        }

        // Wishlist click
        holder.wishlist.setOnClickListener {
            val customerId = SessionManager.getCustomerId(holder.itemView.context) ?: ""

            viewModel.addToWishlist(
                product.productId,
                customerId,
                onResult = { success, message ->
                    if (success) {
                        // Toggle wishlist state
                        product.favorite = !product.favorite
                        notifyItemChanged(position)

                        Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(holder.itemView.context, "Failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Product detail click
        holder.itemView.setOnClickListener {
            ProductDetailPage.launch(holder.itemView.context, product.productId)

        }
    }

    override fun getItemCount(): Int = products.size
}
