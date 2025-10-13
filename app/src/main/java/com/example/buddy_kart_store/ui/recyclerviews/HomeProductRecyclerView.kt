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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.FeaturedProduct
import com.example.buddy_kart_store.ui.Home.ProductDetailPage
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.SessionManager

class HomeProductRecyclerView(
    private val products: MutableList<FeaturedProduct>,
    private val viewModel: fetchCartVM,
    private val wishViewModel: WishListVM

) :
    RecyclerView.Adapter<HomeProductRecyclerView.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.homeproductImage)
        val nameTextView: TextView = itemView.findViewById(R.id.homeproductName)
        val desctxt: TextView = itemView.findViewById(R.id.desctxt)
        val disPriceTextView: TextView = itemView.findViewById(R.id.homeproductdisPrice)

        val button: Button = itemView.findViewById(R.id.homeAddButton)
        val wishlist: ImageButton = itemView.findViewById(R.id.wishlist)
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

        // Set product info
        holder.nameTextView.text = product.name
        holder.disPriceTextView.text = "${product.price}"
        holder.desctxt.text = product.description

        Glide.with(context)
            .load(product.image)
            .placeholder(R.drawable.download)
            .into(holder.imageView)

        // Open product detail
        holder.itemView.setOnClickListener {
            ProductDetailPage.launch(context, product.productId)
        }

        // ---------------- Wishlist toggle ----------------
        product.Wished = sharedPref.isInWishlist(product.productId.toInt())

        fun updateWishlistIcon() {
            if (product.Wished) {
                holder.wishlist.setImageResource(R.drawable.addedwishlist)
                holder.wishlist.setColorFilter(
                    ContextCompat.getColor(context, R.color.red),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                holder.wishlist.setImageResource(R.drawable.fav)
                holder.wishlist.colorFilter = null
            }
        }

        updateWishlistIcon() // initial state

        holder.wishlist.setOnClickListener {
            val customerId = SessionManager.getCustomerId(context) ?: return@setOnClickListener

            if (product.Wished) {
                // Remove from wishlist
                wishViewModel.removeFromWishlist(
                    product.productId,
                    customerId,
                    onResult = { success, message ->
                        if (success) {
                            product.Wished = false
                            sharedPref.removeFromWishlist(product.productId.toInt())
                            notifyItemChanged(position)
                            Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to remove: $message", Toast.LENGTH_SHORT).show()
                        }
                    }

                )
            } else {
                // Add to wishlist
                wishViewModel.addToWishlist(
                    product.productId,
                    customerId,
                    onResult = { success, message ->
                        if (success) {
                            product.Wished = true
                            sharedPref.addToWishlist(product.productId.toInt())
                            notifyItemChanged(position)
                            Toast.makeText(context, "Added to wishlist", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to add: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        // ---------------- Add to Cart ----------------
        holder.button.setOnClickListener {
            val customerId = SessionManager.getCustomerId(context) ?: ""
            val sessionId = SessionManager.getSessionId(context) ?: ""

            viewModel.addToCart(customerId, sessionId, product.productId)
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newProducts: List<FeaturedProduct>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

}
