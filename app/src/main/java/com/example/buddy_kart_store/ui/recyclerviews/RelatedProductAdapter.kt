package com.example.buddy_kart_store.ui.recyclerviews

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
import com.example.buddy_kart_store.model.retrofit_setup.login.RelatedProduct
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.SessionManager

class RelatedProductAdapter(
    private var productList: List<RelatedProduct>,
    private val onItemClick: (RelatedProduct) -> Unit,
    private val wishViewModel: WishListVM,
    private var viewModel : fetchCartVM

) : RecyclerView.Adapter<RelatedProductAdapter.RelatedViewHolder>() {

    private lateinit var context : View
    private var productQuantities = mutableMapOf<String, Int>()


    inner class RelatedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.homeproductImage)
        val productName: TextView = itemView.findViewById(R.id.homeproductName)
        val productPrice: TextView = itemView.findViewById(R.id.homeproductdisPrice)
        val wishlist: ImageView = itemView.findViewById(R.id.wishlist)
        val productDesc: TextView = itemView.findViewById(R.id.desctxt)

        val button: Button = itemView.findViewById(R.id.homeAddButton)

        val quantityControls: View = itemView.findViewById(R.id.quantityControls)
        val decreaseQty: ImageButton = itemView.findViewById(R.id.decreaseQty)
        val productQuantity: TextView = itemView.findViewById(R.id.qtyText)
        val increaseQty: ImageButton = itemView.findViewById(R.id.increaseQty)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelatedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.homepage_product, parent, false)
        return RelatedViewHolder(view)
    }

    override fun onBindViewHolder(holder: RelatedViewHolder, position: Int) {
        val product = productList[position]

        val quantityCount = Sharedpref.CartPref.getCartQuantities(holder.itemView.context)
        Log.d("gettingquantity", "onBindViewHolder: $quantityCount")

        for((productId, qty) in quantityCount){
            productQuantities[productId] = qty

            if(product.productId == productId){
                holder.productQuantity.text = qty.toString()

            }
        }

        holder.productName.text = product.name
        holder.productDesc.text = product.description
        holder.productPrice.text = "$${product.price}"

        Glide.with(holder.itemView.context)
            .load(product.image)
            .placeholder(R.drawable.ic_placeholder) // add placeholder
            .into(holder.productImage)


        holder.itemView.setOnClickListener {
            onItemClick(product)
        }

        if (product.Wished) {
            holder.wishlist.setImageResource(R.drawable.addedwishlist)
            holder.wishlist.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, R.color.red),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            holder.wishlist.setImageResource(R.drawable.fav)
            holder.wishlist.colorFilter = null
        }


        holder.wishlist.setOnClickListener {
            val customerId = SessionManager.getCustomerId(holder.itemView.context) ?: ""

            wishViewModel.addToWishlist(
                product.productId,
                customerId,
                onResult = { success, message ->
                    if (success) {
                        product.Wished = !product.Wished
                        notifyItemChanged(position)

                        Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            holder.itemView.context,
                            "Failed: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }


//        ----------add to cart ---------
        val cartIds = Sharedpref.CartPrefs.getCartIds(holder.itemView.context)
        if (cartIds.contains(product.productId)) {
            holder.button.visibility = View.GONE
            holder.quantityControls.visibility = View.VISIBLE
        } else {
            holder.button.visibility = View.VISIBLE
            holder.quantityControls.visibility = View.GONE
        }

        // ---------------- Add to Cart ----------------
        holder.button.setOnClickListener {
            val customerId = SessionManager.getCustomerId(holder.itemView.context) ?: ""
            val sessionId = SessionManager.getSessionId(holder.itemView.context) ?: ""

            holder.button.isEnabled = false
            holder.button.text = "Adding..."

            viewModel.addToCart(customerId, sessionId, product.productId) { success, message ->
                holder.button.isEnabled = true
                holder.button.text = "Add"

                if (success) {
                    // Save only cart_id
                    Sharedpref.CartPrefs.saveCartIds(holder.itemView.context, setOf(product.productId))
                    Sharedpref.CartPrefs.addProductId(holder.itemView.context, product.productId)



                    holder.button.visibility = View.GONE
                    holder.quantityControls.visibility = View.VISIBLE

                    productQuantities[product.productId] = 1

//                    Toast.makeText(context, "${product.name} Added to cart", Toast.LENGTH_SHORT)
//                        .show()
                } else {
//                    Toast.makeText(context, "Failed to add: $message", Toast.LENGTH_SHORT).show()
                }
            }

            viewModel.fetchCart(customerId, sessionId)

        }

        // ---------------- Quantity controls ----------------

// Cache customer/session IDs once
        val customerId = SessionManager.getCustomerId(holder.itemView.context) ?: ""
        val sessionId = SessionManager.getSessionId(holder.itemView.context) ?: ""

        // Helper function to update quantity
        fun updateQuantity(newQty: Int) {
            productQuantities[product.productId] = newQty
            holder.productQuantity.text = newQty.toString()

            val cartId = Sharedpref.CartPref.getCartIdForProduct(holder.itemView.context, product.productId)
            if (cartId != null) {
                // Call ViewModel to update backend
                viewModel.updateCartQuantity(customerId, sessionId, cartId, newQty)

                // Save locally, merge with existing map
                Sharedpref.CartPref.saveCartQuantities(
                    holder.itemView.context,
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
                holder.button.isEnabled = false


                val cartId = Sharedpref.CartPref.getCartIdForProduct(holder.itemView.context, product.productId)
                if (cartId != null) {
                    viewModel.removeProductFromCart(cartId, holder.itemView.context)
                }

                // Remove from local map & SharedPref
                productQuantities.remove(product.productId)
                Sharedpref.CartPref.clearCart(holder.itemView.context )
            }
        }




    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<RelatedProduct>) {
        productList = newList
        notifyDataSetChanged()
    }
}
