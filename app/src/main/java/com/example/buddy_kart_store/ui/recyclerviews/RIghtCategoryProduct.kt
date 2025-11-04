package com.example.buddy_kart_store.ui.recyclerviews

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.CategoryProduct
import com.example.buddy_kart_store.ui.Home.ProductDetailPage
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.SessionManager

class RIghtCategoryProduct(
    private val products: MutableList<CategoryProduct>,
    private var viewModel: fetchCartVM
) : RecyclerView.Adapter<RIghtCategoryProduct.ViewHolder>() {

    // Cached data (avoid SharedPref calls in scroll)
    private var cartQuantities = mutableMapOf<String, Int>()
    private var ProductQuantity = mutableMapOf<String, Int>()

    private var cartIds = mutableSetOf<String>()

    // Single Glide instance
    private var glide: RequestManager? = null

    fun preloadSharedData(context: android.content.Context) {
        cartQuantities = Sharedpref.CartPref.getCartQuantities(context).toMutableMap()
        cartIds = Sharedpref.CartPrefs.getCartIds(context).toMutableSet()
        glide = Glide.with(context)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.productImage)
        val name: TextView = view.findViewById(R.id.productName)
        val price: TextView = view.findViewById(R.id.productPrice)
        val badge: TextView = view.findViewById(R.id.discountBadge)
        val discount: TextView = view.findViewById(R.id.discount)
        val addButton: TextView = view.findViewById(R.id.homeAddButton)
        val quantityControls: View = view.findViewById(R.id.quantityControls)
        val decreaseQty: ImageButton = view.findViewById(R.id.decreaseQty)
        val productQuantity: TextView = view.findViewById(R.id.qtyText)
        val increaseQty: ImageButton = view.findViewById(R.id.increaseQty)

        init {
            // Product click → show loader → open PDP
            view.setOnClickListener {
                val pos = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?: return@setOnClickListener
                val product = products[pos]
                val context = view.context

                val dialog = android.app.Dialog(context)
                dialog.setContentView(R.layout.dialog_loader)
                dialog.setCancelable(false)
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()

                view.postDelayed({
                    dialog.dismiss()
                    ProductDetailPage.launch(context, product.productId.toString())
                }, 1000)
            }

            // Add to cart
            addButton.setOnClickListener {
                val pos = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?: return@setOnClickListener
                val context = view.context
                val product = products[pos]
                val customerId = SessionManager.getCustomerId(context) ?: ""
                val sessionId = SessionManager.getSessionId(context) ?: ""

                addButton.isEnabled = false
                addButton.text = "Adding..."

                viewModel.addToCart(customerId, sessionId, product.productId.toString()) { success, message ->
                    addButton.isEnabled = true
                    addButton.text = "Add"

                    if (success) {
                        cartIds.add(product.productId.toString())
                        cartQuantities[product.productId.toString()] = 1
                        Sharedpref.CartPrefs.addProductId(context, product.productId.toString())

                        addButton.visibility = View.GONE
                        quantityControls.visibility = View.VISIBLE
                        productQuantity.text = "1"
                        Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }

                // Fetch cart once per add (light call)
                viewModel.fetchCart(customerId, sessionId)
            }

            increaseQty.setOnClickListener {
                val pos = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?: return@setOnClickListener
                val context = view.context
                val product = products[pos]
                val newQty = (cartQuantities[product.productId.toString()] ?: 1) + 1
                updateQuantity(context, product, newQty)
            }

            decreaseQty.setOnClickListener {
                val pos = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?: return@setOnClickListener
                val context = view.context
                val product = products[pos]
                val oldQty = cartQuantities[product.productId.toString()] ?: 1

                if (oldQty > 1) {
                    updateQuantity(context, product, oldQty - 1)
                } else {
                    val cartId = Sharedpref.CartPref.getCartIdForProduct(context, product.productId.toString())
                    if (cartId != null) viewModel.removeProductFromCart(cartId, context)

                    cartQuantities.remove(product.productId.toString())
                    cartIds.remove(product.productId.toString())
                    Sharedpref.CartPref.deleteProduct(context, product.productId.toString())
                    Sharedpref.CartPrefs.removeProductId(context, product.productId.toString())

                    addButton.visibility = View.VISIBLE
                    quantityControls.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.category_product_card, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        val context = holder.itemView.context

        holder.name.text = product.name

        val mainPrice = product.price.toDoubleOrNull() ?: 0.0        // special or offer price
        val actualPrice = product.discount.toDoubleOrNull() ?: 0.0   // original price

        if (mainPrice > 0.0 && actualPrice > 0.0 && actualPrice > mainPrice) {
            holder.price.text = "₹${String.format("%.2f", mainPrice)}"
            holder.discount.text = "₹${String.format("%.2f", actualPrice)}"
            holder.discount.setTypeface(null, android.graphics.Typeface.BOLD)




            holder.discount.paintFlags = holder.discount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.discount.visibility = View.VISIBLE

            val discountPercent = ((actualPrice - mainPrice) / actualPrice) * 100
            holder.badge.text = "-${String.format("%.0f", discountPercent)}% OFF"
            holder.badge.visibility = View.VISIBLE

            holder.price.setTypeface(null, android.graphics.Typeface.BOLD)

        } else {
            val shownPrice = if (mainPrice > 0.0) mainPrice else actualPrice
            holder.price.text = "₹${String.format("%.2f", shownPrice)}"

            holder.discount.visibility = View.GONE
            holder.badge.visibility = View.GONE

            // Make price bold
            holder.price.setTypeface(null, android.graphics.Typeface.BOLD)
        }


        Glide.with(holder.itemView)
            .load(product.imageUrl)
            .thumbnail(0.25f)
            .placeholder(R.drawable.noproduct)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .skipMemoryCache(false)
            .dontAnimate()
            .into(holder.image)


        val isInCart = Sharedpref.CartPref.getCartIdForProduct(
            holder.itemView.context,
            product.productId.toString()
        ) != null
        Log.d("getting", "onBindViewHolder: $isInCart")
        if (isInCart) {
            holder.addButton.visibility = View.GONE
            holder.quantityControls.visibility = View.VISIBLE
            holder.productQuantity.text = (cartQuantities[product.productId.toString()] ?: 1).toString()
        } else {
            holder.addButton.visibility = View.VISIBLE
            holder.quantityControls.visibility = View.GONE
        }
        val quantityCount = Sharedpref.CartPref.getCartQuantities(context)
        Log.d("gettingquantity", "onBindViewHolder: $quantityCount")

        for ((productId, qty) in quantityCount) {
            ProductQuantity[productId] = qty

            if (product.productId.toString() == productId) {
                holder.productQuantity.text = qty.toString()

            }
        }
    }

    private fun updateQuantity(
        context: android.content.Context,
        product: CategoryProduct,
        newQty: Int
    ) {
        val customerId = SessionManager.getCustomerId(context) ?: ""
        val sessionId = SessionManager.getSessionId(context) ?: ""
        val cartId = Sharedpref.CartPref.getCartIdForProduct(context, product.productId.toString())

        cartQuantities[product.productId.toString()] = newQty
        Sharedpref.CartPref.saveCartQuantities(context, mapOf(product.productId.toString() to newQty))

        if (cartId != null) {
            viewModel.updateCartQuantity(customerId, sessionId, cartId, newQty)
        } else {
            Log.e("CartUpdate", "Missing cartId for ${product.productId}")
        }

        val idx = products.indexOf(product)
        if (idx >= 0) notifyItemChanged(idx, "quantityChanged")
    }

    override fun getItemCount(): Int = products.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newProducts: List<CategoryProduct>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}
