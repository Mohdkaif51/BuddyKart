package com.example.buddy_kart_store.ui.recyclerviews

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.TrendingProduct
import com.example.buddy_kart_store.ui.Home.ProductDetailPage
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.SessionManager

class WishlistRecycler(
    private var productList: MutableList<TrendingProduct>,
    private val listener: WishlistListener,
    private val fetchCart: fetchCartVM
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
        val button: AppCompatButton = itemView.findViewById(R.id.homeAddButton)
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

        // Product info
        holder.productName.text = product.name
        holder.productPrice.text = product.price
        holder.discount.visibility = View.GONE

        // Function to update wishlist icon
        fun updateWishlistIcon(isFavorite: Boolean) {
            if (isFavorite) {
                holder.wishlist.setImageResource(R.drawable.addedwishlist)
                holder.wishlist.setColorFilter(
                    ContextCompat.getColor(context, R.color.red),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                holder.wishlist.setImageResource(R.drawable.fav)
                holder.wishlist.clearColorFilter()
            }
        }

        // Initial wishlist state
        var isInWishlist = Sharedpref.WishlistPrefs.isInWishlist(context, product.product_id)
        updateWishlistIcon(isInWishlist)

        // Wishlist toggle click
        holder.wishlist.setOnClickListener {
            isInWishlist = !isInWishlist

            if (isInWishlist) {
                // Add to wishlist
                Sharedpref.WishlistPrefs.addProductId(context, product.product_id)
                updateWishlistIcon(true)

                listener.addToWishlist(product.product_id, position) { success ->
                    if (!success) {
                        // Revert if API fails
                        isInWishlist = false
                        Sharedpref.WishlistPrefs.removeProductId(context, product.product_id)
                        updateWishlistIcon(false)
                    }
                }
            } else {
                // Remove from wishlist
                Sharedpref.WishlistPrefs.removeProductId(context, product.product_id)
                updateWishlistIcon(false)

                listener.removeFromWishlist(product.product_id, position) { success ->
                    if (!success) {
                        // Revert if API fails
                        isInWishlist = true
                        Sharedpref.WishlistPrefs.addProductId(context, product.product_id)
                        updateWishlistIcon(true)
                    }
                }
            }
        }

        // Product click
        holder.itemView.setOnClickListener {
            ProductDetailPage.launch(context, product.product_id)
        }

        // Add to cart
        holder.button.setOnClickListener {
            val customerId = SessionManager.getCustomerId(context) ?: ""
            val sessionId = SessionManager.getSessionId(context) ?: ""

            holder.button.isEnabled = false
            holder.button.text = "Adding..."

            fetchCart.addToCart(customerId, sessionId, product.product_id) { success, message ->
                holder.button.isEnabled = true
                holder.button.text = "Add"

                if (success) {
                    Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to add: $message", Toast.LENGTH_SHORT).show()
                }
            }
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

