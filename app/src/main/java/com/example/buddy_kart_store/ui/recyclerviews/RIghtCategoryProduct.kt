package com.example.buddy_kart_store.ui.recyclerviews

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.CategoryProduct
import com.example.buddy_kart_store.ui.Home.ProductDetailPage
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.SessionManager

class RIghtCategoryProduct(
    private val products: List<CategoryProduct>,
    private val viewModel: fetchCartVM
) :
    RecyclerView.Adapter<RIghtCategoryProduct.ViewHolder>() {

    private var productQuantities = mutableMapOf<String, Int>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.productImage)
        val name: TextView = itemView.findViewById(R.id.productName)
        val price: TextView = itemView.findViewById(R.id.productPrice)
        val discount: TextView = itemView.findViewById(R.id.discount)
        val button = itemView.findViewById<TextView>(R.id.homeAddButton)


        val quantityControls: View = itemView.findViewById(R.id.quantityControls)
        val decreaseQty: ImageButton = itemView.findViewById(R.id.decreaseQty)
        val productQuantity: TextView = itemView.findViewById(R.id.qtyText)
        val increaseQty: ImageButton = itemView.findViewById(R.id.increaseQty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_product_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        val context = holder.itemView.context
        val productIdStr = product.productId.toString() // safe non-null

        // ---------------- Initialize quantity ----------------
        val quantityCount = Sharedpref.CartPref.getCartQuantities(context)
        val currentQty = quantityCount[productIdStr] ?: 0
        productQuantities[productIdStr] = currentQty
        holder.productQuantity.text = if (currentQty > 0) currentQty.toString() else "1"

        // ---------------- Set product info ----------------
        holder.name.text = product.name
        holder.price.text = "₹${product.price}"
        holder.discount.text = "₹${product.discount}"
        holder.discount.visibility = View.GONE
        holder.discount.paintFlags =
            holder.discount.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG

        Glide.with(context)
            .load(product.imageUrl)
            .placeholder(R.drawable.download)
            .error(R.drawable.download)
            .into(holder.image)

        // ---------------- Cart button vs quantity controls ----------------
        val cartIds = Sharedpref.CartPrefs.getCartIds(context)
        val inCart = cartIds.contains(productIdStr)

        holder.button.visibility = if (inCart) View.GONE else View.VISIBLE
        holder.quantityControls.visibility = if (inCart) View.VISIBLE else View.GONE

        // ---------------- Add to Cart ----------------
        holder.button.setOnClickListener {
            val customerId = SessionManager.getCustomerId(context) ?: ""
            val sessionId = SessionManager.getSessionId(context) ?: ""

            holder.button.isEnabled = false
            holder.button.text = "Adding..."

            viewModel.addToCart(customerId, sessionId, productIdStr) { success, message ->
                holder.button.isEnabled = true
                holder.button.text = "Add"

                if (success) {
                    // Save product in SharedPref
                    Sharedpref.CartPrefs.addProductId(context, productIdStr)
                    Sharedpref.CartPrefs.saveCartIds(context, setOf(productIdStr))

                    holder.button.visibility = View.GONE
                    holder.quantityControls.visibility = View.VISIBLE

                    // Initialize quantity
                    productQuantities[productIdStr] = 1
                    Sharedpref.CartPref.saveCartQuantities(context, mapOf(productIdStr to 1))

                    Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to add: $message", Toast.LENGTH_SHORT).show()
                }
            }

            viewModel.fetchCart(SessionManager.getCustomerId(context) ?: "", SessionManager.getSessionId(context) ?: "")
        }

        // ---------------- Helper function ----------------
        fun updateQuantity(newQty: Int) {
            productQuantities[productIdStr] = newQty
            holder.productQuantity.text = newQty.toString()

            val cartId = Sharedpref.CartPref.getCartIdForProduct(context, productIdStr)
            if (cartId != null) {
                // Update backend
                viewModel.updateCartQuantity(SessionManager.getCustomerId(context) ?: "",
                    SessionManager.getSessionId(context) ?: "",
                    cartId,
                    newQty
                )

                // Merge quantity map instead of overwriting
                val existingQuantities = Sharedpref.CartPref.getCartQuantities(context).toMutableMap()
                existingQuantities[productIdStr] = newQty
                Sharedpref.CartPref.saveCartQuantities(context, existingQuantities)
            } else {
                Log.e("CartUpdate", "No cartId found for product: $productIdStr")
            }
        }

        // ---------------- Increase / Decrease ----------------
        holder.increaseQty.setOnClickListener {
            val oldQty = productQuantities[productIdStr] ?: 1
            updateQuantity(oldQty + 1)
        }

        holder.decreaseQty.setOnClickListener {
            val oldQty = productQuantities[productIdStr] ?: 1
            if (oldQty > 1) {
                updateQuantity(oldQty - 1)
            } else {
                // Remove product from cart
//                holder.button.visibility = View.VISIBLE
//                holder.quantityControls.visibility = View.GONE
                holder.button.isEnabled = false

                val cartId = Sharedpref.CartPref.getCartIdForProduct(context, productIdStr)
                if (cartId != null) {
                    viewModel.removeProductFromCart(cartId, context)
                }

                // Remove only this product from SharedPref
                productQuantities.remove(productIdStr)
                Sharedpref.CartPref.deleteProduct(context, productIdStr)
                Sharedpref.CartPrefs.removeProductId(context, productIdStr)
            }
        }

        // ---------------- Item click ----------------
        holder.itemView.setOnClickListener {
            ProductDetailPage.launch(context, productIdStr)
        }
    }

    override fun getItemCount(): Int = products.size
}
