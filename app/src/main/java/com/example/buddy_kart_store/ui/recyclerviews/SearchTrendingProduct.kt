package com.example.buddy_kart_store.ui.recyclerviews

import android.R.id.message
import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.SearchProduct
import com.example.buddy_kart_store.ui.Home.ProductDetailPage
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.SessionManager

class SearchTrendingProduct(
    private val products: MutableList<SearchProduct>,
    private val viewModel: WishListVM,
    private val cartVM: fetchCartVM
) : RecyclerView.Adapter<SearchTrendingProduct.ProductViewHolder>() {

    private var productQuantities = mutableMapOf<String, Int>()


    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wishlist: ImageView = itemView.findViewById(R.id.btnWishlist)
        val image: ImageView = itemView.findViewById(R.id.productImage)
        var name: TextView = itemView.findViewById(R.id.productName)
        val price: TextView = itemView.findViewById(R.id.productPrice)
        val badge: TextView = itemView.findViewById(R.id.discountBadge)
        val actual: TextView = itemView.findViewById(R.id.productActualPrice)
        val button: AppCompatButton = itemView.findViewById(R.id.homeAddButton)
        val quantityControls: View = itemView.findViewById(R.id.quantityControls)
        val decreaseQty: ImageButton = itemView.findViewById(R.id.decreaseQty)
        val productQuantity: TextView = itemView.findViewById(R.id.qtyText)
        val increaseQty: ImageButton = itemView.findViewById(R.id.increaseQty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trending_product, parent, false)
        return ProductViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        val context = holder.itemView.context

        val quantityCount = Sharedpref.CartPref.getCartQuantities(context)
        Log.d("gettingquantity", "onBindViewHolder: $quantityCount")

        for((productId, qty) in quantityCount){
            productQuantities[productId] = qty

            if(product.productId == productId){
                holder.productQuantity.text = qty.toString()

            }
        }
        holder.name.text = product.name


        val specialValue = product.special.toDoubleOrNull() ?: 0.0
        val actualValue = product.price.toDoubleOrNull() ?: 0.0

        val disc = (actualValue - specialValue) / actualValue * 100
        holder.badge.text = "-${String.format("%.0f", disc)}% OFF"

        if (specialValue == 0.0) {
            // 🟢 No discount — show only actual price
            holder.price.text = "₹${String.format("%.2f", actualValue)}"
            holder.actual.visibility = View.GONE
            holder.price.paintFlags = holder.price.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        } else {
            // 🟢 Discount available — show both prices
            holder.price.text = "₹${String.format("%.2f", specialValue)}"
            holder.actual.text = "₹${String.format("%.2f", actualValue)}"
            holder.actual.paintFlags = holder.actual.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.actual.visibility = View.VISIBLE
        }



        // Inside onBindViewHolder
        val isInWishlist = Sharedpref.WishlistPrefs.isInWishlist(context, product.productId)
        product.favorite = isInWishlist
        updateWishlistIcon(holder, product.favorite)

        holder.wishlist.setOnClickListener {
            if (product.favorite) {
                // Remove
                Sharedpref.WishlistPrefs.removeProductId(context, product.productId)
                updateWishlistIcon(holder, false)
                product.favorite = false

                viewModel.removeFromWishlist(
                    product.productId,
                    SessionManager.getCustomerId(context) ?: ""
                ) { success, _ ->
                    if (!success) {
                        // Revert on failure
                        Sharedpref.WishlistPrefs.addProductId(context, product.productId)
                        updateWishlistIcon(holder, true)
                        product.favorite = true
                    }
                }
            } else {
                // Add
                Sharedpref.WishlistPrefs.addProductId(context, product.productId)
                updateWishlistIcon(holder, true)
                product.favorite = true

                viewModel.addToWishlist(
                    product.productId,
                    SessionManager.getCustomerId(context) ?: ""
                ) { success, _ ->
                    if (!success) {
                        // Revert on failure
                        Sharedpref.WishlistPrefs.removeProductId(context, product.productId)
                        updateWishlistIcon(holder, false)
                        product.favorite = false
                    }
                }
            }
        }
        // --- Product click ---
        holder.itemView.setOnClickListener {
            ProductDetailPage.launch(context, product.productId)
        }

        // --- Add to cart click ---
        val cartIds = Sharedpref.CartPrefs.getCartIds(context)
        if (cartIds.contains(product.productId)) {
            holder.button.visibility = View.GONE
            holder.quantityControls.visibility = View.VISIBLE
        } else {
            holder.button.visibility = View.VISIBLE
            holder.quantityControls.visibility = View.GONE
        }

        // ---------------- Add to Cart ----------------
        holder.button.setOnClickListener {
            val customerId = SessionManager.getCustomerId(context) ?: ""
            val sessionId = SessionManager.getSessionId(context) ?: ""

            holder.button.isEnabled = false
            holder.button.text = "Adding..."

            cartVM.addToCart(customerId, sessionId, product.productId) { success, message ->
                holder.button.isEnabled = true
                holder.button.text = "Add"

                if (success) {
                    // Save only cart_id
                    Sharedpref.CartPrefs.saveCartIds(context, setOf(product.productId))
                    Sharedpref.CartPrefs.addProductId(context, product.productId)



                    holder.button.visibility = View.GONE
                    holder.quantityControls.visibility = View.VISIBLE

                    productQuantities[product.productId] = 1

                    Toast.makeText(context, "${product.name} Added to cart", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(context, "Failed to add: $message", Toast.LENGTH_SHORT).show()
                }
            }

            cartVM.fetchCart(customerId, sessionId)

        }

        // ---------------- Quantity controls ----------------

// Cache customer/session IDs once
        val customerId = SessionManager.getCustomerId(context) ?: ""
        val sessionId = SessionManager.getSessionId(context) ?: ""

        // Helper function to update quantity
        fun updateQuantity(newQty: Int) {
            productQuantities[product.productId] = newQty
            holder.productQuantity.text = newQty.toString()

            val cartId = Sharedpref.CartPref.getCartIdForProduct(context, product.productId)
            if (cartId != null) {
                // Call ViewModel to update backend
                cartVM.updateCartQuantity(customerId, sessionId, cartId, newQty)

                // Save locally, merge with existing map
                Sharedpref.CartPref.saveCartQuantities(
                    context,
                    mapOf(product.productId to newQty)
                )
            } else {
                Log.e("CartUpdate", "No cartId found for product: ${product.productId}")
            }
        }

// ---------------- Increase Quantity ----------------
        holder.increaseQty.setOnClickListener {
            val oldQty = productQuantities[product.productId] ?: 1
            val newQty = oldQty + 1
            updateQuantity(newQty)
        }

// ---------------- Decrease Quantity ----------------
        holder.decreaseQty.setOnClickListener {
            val oldQty = productQuantities[product.productId] ?: 1

            if (oldQty > 1) {
                val newQty = oldQty - 1
                updateQuantity(newQty)
            } else {
                // If quantity is 1, remove product from cart
                holder.button.isEnabled = false


                val cartId = Sharedpref.CartPref.getCartIdForProduct(context, product.productId)
                if (cartId != null) {
                    cartVM.removeProductFromCart(cartId, context)
                }

                // Remove from local map & SharedPref
                productQuantities.remove(product.productId)
                Sharedpref.CartPref.clearCart(context )
            }
        }


        // --- Load product image ---
        Glide.with(context)
            .load(product.imageUrl)
            .placeholder(R.drawable.noproduct)
            .error(R.drawable.noproduct)
            .into(holder.image)
    }

    // --- Helper function to update wishlist icon ---
    private fun updateWishlistIcon(holder: ProductViewHolder, isFavorite: Boolean) {
        if (isFavorite) {
            holder.wishlist.setImageResource(R.drawable.addedwishlist)
            holder.wishlist.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, R.color.red),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            holder.wishlist.setImageResource(R.drawable.fav)
            holder.wishlist.clearColorFilter()
        }
    }

    override fun getItemCount(): Int = products.size
}
