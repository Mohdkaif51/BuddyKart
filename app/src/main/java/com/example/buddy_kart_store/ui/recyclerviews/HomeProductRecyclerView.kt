package com.example.buddy_kart_store.ui.recyclerviews

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.FeaturedProduct
import com.example.buddy_kart_store.ui.Home.ProductDetailPage
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.SessionManager
import com.example.buddy_kart_store.viewmodel.HomeVm

class HomeProductRecyclerView(
    private val products: MutableList<FeaturedProduct>,
    private val viewModel: fetchCartVM,
    private val wishViewModel: WishListVM
) : RecyclerView.Adapter<HomeProductRecyclerView.ViewHolder>() {


    private var productQuantities = mutableMapOf<String, Int>()


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.homeproductImage)
        val nameTextView: TextView = itemView.findViewById(R.id.homeproductName)
        val desctxt: TextView = itemView.findViewById(R.id.desctxt)
        val disPriceTextView: TextView = itemView.findViewById(R.id.homeproductdisPrice)

        val button: Button = itemView.findViewById(R.id.homeAddButton)
        val wishlist: ImageButton = itemView.findViewById(R.id.wishlist)

        val quantityControls: View = itemView.findViewById(R.id.quantityControls)
        val decreaseQty: ImageButton = itemView.findViewById(R.id.decreaseQty)
        val productQuantity: TextView = itemView.findViewById(R.id.qtyText)
        val increaseQty: ImageButton = itemView.findViewById(R.id.increaseQty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.homepage_product, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        val context = holder.itemView.context
        val sharedPref = Sharedpref(context)

        val quantityCount = Sharedpref.CartPref.getCartQuantities(context)
        Log.d("gettingquantity", "onBindViewHolder: $quantityCount")

        for((productId, qty) in quantityCount){
            productQuantities[productId] = qty

            if(product.productId == productId){
                holder.productQuantity.text = qty.toString()

            }
        }

        // ---------------- Set product info ----------------
        holder.nameTextView.text = product.name
        holder.disPriceTextView.text = product.price
        holder.desctxt.text = product.description

        Glide.with(context)
            .load(product.image)
            .placeholder(R.drawable.download)
            .into(holder.imageView)

        // ---------------- Open product detail ----------------
        holder.itemView.setOnClickListener {
            ProductDetailPage.launch(context, product.productId)
        }

        // ---------------- Wishlist toggle ----------------
        product.Wished = Sharedpref.WishlistPrefs.isInWishlist(context, product.productId)
        fun updateWishlistIcon(isWished: Boolean) {
            if (isWished) {
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
        updateWishlistIcon(product.Wished)

        holder.wishlist.setOnClickListener {
            val customerId = SessionManager.getCustomerId(context) ?: return@setOnClickListener
            val newState = !product.Wished
            product.Wished = newState
            updateWishlistIcon(newState)

            if (newState) {
                Sharedpref.WishlistPrefs.addProductId(context, product.productId)
                wishViewModel.addToWishlist(product.productId, customerId) { success, _ ->
                    if (!success) {
                        product.Wished = false
                        Sharedpref.WishlistPrefs.removeProductId(context, product.productId)
                        updateWishlistIcon(false)
                        Toast.makeText(context, "Failed to add to wishlist", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        wishViewModel.notifyWishlistChanged()
                    }
                }
            } else {
                Sharedpref.WishlistPrefs.removeProductId(context, product.productId)
                wishViewModel.removeFromWishlist(product.productId, customerId) { success, _ ->
                    if (!success) {
                        product.Wished = true
                        Sharedpref.WishlistPrefs.addProductId(context, product.productId)
                        updateWishlistIcon(true)
                        Toast.makeText(
                            context,
                            "Failed to remove from wishlist",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        wishViewModel.notifyWishlistChanged()
                    }
                }
            }
        }

        // ---------------- Check Cart status ----------------
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

            viewModel.addToCart(customerId, sessionId, product.productId) { success, message ->
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

            viewModel.fetchCart(customerId, sessionId)

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
                viewModel.updateCartQuantity(customerId, sessionId, cartId, newQty)

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
                    viewModel.removeProductFromCart(cartId, context)
                }

                // Remove from local map & SharedPref
                productQuantities.remove(product.productId)
                Sharedpref.CartPref.clearCart(context )
            }
        }


    }

    override fun getItemCount(): Int = products.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newProducts: List<FeaturedProduct>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }


}
