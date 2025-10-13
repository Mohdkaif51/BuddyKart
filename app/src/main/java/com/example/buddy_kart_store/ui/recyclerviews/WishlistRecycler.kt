package com.example.buddy_kart_store.ui.recyclerviews

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.TrendingProduct
import com.example.buddy_kart_store.ui.Home.ProductDetailPage
import com.example.buddy_kart_store.utils.Sharedpref

class WishlistRecycler(
    private var productList: MutableList<TrendingProduct>,
    private val listener: WishlistListener
) : RecyclerView.Adapter<WishlistRecycler.ViewHolder>() {

    interface WishlistListener {
        fun addToWishlist(productId: String, position: Int, onResult: (Boolean) -> Unit)
        fun removeFromWishlist(productId: String, position: Int, onResult: (Boolean) -> Unit)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val discount: TextView = itemView.findViewById(R.id.discountBadge)
        val wishlist: ImageView = itemView.findViewById(R.id.btnWishlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trending_product, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        val context = holder.itemView.context
        val sharedPref = Sharedpref(context)

        // Product info
        holder.productName.text = product.name
        holder.productPrice.text = product.price
        holder.discount.visibility = View.GONE

        // Determine wishlist state
        var isInWishlist = sharedPref.isInWishlist(product.product_id.toInt())

        // Function to update UI of wishlist icon
        fun updateWishlistIcon(state: Boolean) {
            if (state) {
                holder.wishlist.setImageResource(R.drawable.addedwishlist)
                holder.wishlist.setColorFilter(
                    ContextCompat.getColor(context, R.color.red),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                holder.wishlist.setImageResource(R.drawable.fav)
                holder.wishlist.colorFilter = null
            }
        }

        // Initial icon update
        updateWishlistIcon(isInWishlist)

        // Wishlist toggle
        holder.wishlist.setOnClickListener {
            if (isInWishlist) {
                listener.removeFromWishlist(product.product_id, position) { success ->
                    if (success) {
                        sharedPref.removeFromWishlist(product.product_id.toInt())
                        isInWishlist = false
                        updateWishlistIcon(false)
                    }
                }
            } else {
                listener.addToWishlist(product.product_id, position) { success ->
                    if (success) {
                        sharedPref.addToWishlist(product.product_id.toInt())
                        isInWishlist = true
                        updateWishlistIcon(true)
                    }
                }
            }
        }

        // Product click
        holder.itemView.setOnClickListener {
            ProductDetailPage.launch(context, product.product_id)
        }

        // Load image
        Glide.with(context)
            .load(product.imageUrl)
            .placeholder(R.drawable.download)
            .error(R.drawable.download)
            .into(holder.productImage)
    }

    override fun getItemCount(): Int = productList.size

    fun updateData(newList: List<TrendingProduct>) {
        productList.clear()
        productList.addAll(newList)
        notifyDataSetChanged()
    }
}

