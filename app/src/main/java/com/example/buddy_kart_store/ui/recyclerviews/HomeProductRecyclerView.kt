package com.example.buddy_kart_store.ui.recyclerviews

import android.annotation.SuppressLint
import android.graphics.Paint
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
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.FeaturedProduct
import com.example.buddy_kart_store.ui.Home.ProductDetailPage
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.SessionManager

class HomeProductRecyclerView(
    private val products: MutableList<FeaturedProduct>,
    private var viewModel: fetchCartVM,
    private var wishViewModel: WishListVM
) : RecyclerView.Adapter<HomeProductRecyclerView.ViewHolder>() {

    // Cached data to avoid SharedPref access during onBind
    private var cartQuantities = mutableMapOf<String, Int>()
    private var wishlistIds = mutableSetOf<String>()
    private var cartIds = mutableSetOf<String>()

    private var ProductQuantity = mutableMapOf<String, Int>()



    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.homeproductImage)
        val nameTextView: TextView = view.findViewById(R.id.homeproductName)
        val descText: TextView = view.findViewById(R.id.desctxt)
        val priceStrike: TextView = view.findViewById(R.id.homeproductdPrice)
        val priceText: TextView = view.findViewById(R.id.homeproductdisPrice)
        val badge: TextView = view.findViewById(R.id.discountBadge)
        val addBtn: Button = view.findViewById(R.id.homeAddButton)
        val wishlist: ImageButton = view.findViewById(R.id.wishlist)
        val qtyControls: View = view.findViewById(R.id.quantityControls)
        val decreaseQty: ImageButton = view.findViewById(R.id.decreaseQty)
        val productQty: TextView = view.findViewById(R.id.qtyText)
        val increaseQty: ImageButton = view.findViewById(R.id.increaseQty)

        init {

            view.setOnClickListener {
                val pos = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return@setOnClickListener
                val product = products[pos]


                val context = view.context
                val dialog = android.app.Dialog(context)
                dialog.setContentView(R.layout.dialog_loader)
                dialog.setCancelable(false)
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()

                // Launch PDP after a slight delay to let loader appear
                view.postDelayed({
                    dialog.dismiss()
                    ProductDetailPage.launch(context, product.productId)
                }, 1000)
            }


            // Wishlist toggle (keeps previous optimized logic)
            wishlist.setOnClickListener {
                val pos = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?: return@setOnClickListener
                val context = view.context
                val product = products[pos]
                val customerId = SessionManager.getCustomerId(context) ?: return@setOnClickListener

                val newState = !product.Wished
                product.Wished = newState
                updateWishlistIcon(wishlist, context, newState)

                if (newState) {
                    wishlistIds.add(product.productId)
                    Sharedpref.WishlistPrefs.addProductId(context, product.productId)
                    wishViewModel.addToWishlist(product.productId, customerId) { success, _ ->
                        if (!success) {
                            product.Wished = false
                            wishlistIds.remove(product.productId)
                            Sharedpref.WishlistPrefs.removeProductId(context, product.productId)
                            updateWishlistIcon(wishlist, context, false)
                            Toast.makeText(context, "Failed to add to wishlist", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    wishlistIds.remove(product.productId)
                    Sharedpref.WishlistPrefs.removeProductId(context, product.productId)
                    wishViewModel.removeFromWishlist(product.productId, customerId) { success, _ ->
                        if (!success) {
                            product.Wished = true
                            wishlistIds.add(product.productId)
                            Sharedpref.WishlistPrefs.addProductId(context, product.productId)
                            updateWishlistIcon(wishlist, context, true)
                            Toast.makeText(
                                context,
                                "Failed to remove from wishlist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            // ------------------ ADD TO CART (restored from your working code + improvements) ------------------
            addBtn.setOnClickListener {
                val pos = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?: return@setOnClickListener
                val context = view.context
                val product = products[pos]
                val customerId = SessionManager.getCustomerId(context) ?: ""
                val sessionId = SessionManager.getSessionId(context) ?: ""

                // disable + UX
                addBtn.isEnabled = false
                addBtn.text = "Adding..."

                viewModel.addToCart(customerId, sessionId, product.productId) { success, message ->
                    // restore button state
                    addBtn.isEnabled = true
                    addBtn.text = "Add"

                    if (success) {
                        // Update local caches + SharedPref (don't overwrite whole set)
                        cartIds.add(product.productId)
                        cartQuantities[product.productId] = 1
                        Sharedpref.CartPrefs.addProductId(context, product.productId)
                        // show qty controls
                        addBtn.visibility = View.GONE
                        qtyControls.visibility = View.VISIBLE
                        productQty.text = "1"
                        Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }

                // refresh server-side cart if needed (keeps your original fetch)
                viewModel.fetchCart(customerId, sessionId)
            }

            // ------------------ INCREASE QUANTITY (restored) ------------------
            increaseQty.setOnClickListener {
                val pos = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?: return@setOnClickListener
                val context = view.context
                val product = products[pos]
                val newQty = (cartQuantities[product.productId] ?: 1) + 1
                updateQuantity(context, product, newQty)
            }

            // ------------------ DECREASE QUANTITY (restored with safe delete) ------------------
            decreaseQty.setOnClickListener {
                val pos = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?: return@setOnClickListener
                val context = view.context
                val product = products[pos]
                val oldQty = cartQuantities[product.productId] ?: 1
                if (oldQty > 1) {
                    updateQuantity(context, product, oldQty - 1)
                } else {
                    // If quantity was 1 -> remove product from cart
                    val cartId = Sharedpref.CartPref.getCartIdForProduct(context, product.productId)
                    if (cartId != null) {
                        viewModel.removeProductFromCart(cartId, context)
                    }

                    cartQuantities.remove(product.productId)
                    cartIds.remove(product.productId)

                    // Prefer deleting the single product entry rather than clearing whole cart
                    // (your code used clearCart in some places; using deleteProduct avoids accidental clears)
                    Sharedpref.CartPref.deleteProduct(context, product.productId)

                    addBtn.visibility = View.VISIBLE
                    qtyControls.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.homepage_product, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        val context = holder.itemView.context

        // Product info
        holder.nameTextView.text = product.name
        holder.descText.text = product.description

        val price = product.price.toDoubleOrNull() ?: 0.0
        val actualPrice = product.actualPrice

        // Discount badge
        if (actualPrice > 0 && price < actualPrice) {
            val discount = ((actualPrice - price) / actualPrice * 100).toInt()
            holder.badge.text = "-${discount}% OFF"
            holder.badge.visibility = View.VISIBLE
        } else {
            holder.badge.visibility = View.GONE
        }

        // Price display
        if (price == 0.0) {
            holder.priceText.text = "₹${String.format("%.2f", actualPrice)}"
            holder.priceStrike.text = ""
        } else {
            holder.priceText.text = "₹${String.format("%.2f", price)}"
            holder.priceStrike.text = "₹${String.format("%.2f", actualPrice)}"
            holder.priceStrike.paintFlags =
                holder.priceStrike.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }



        Glide.with(holder.imageView)
            .load(product.image)
            .thumbnail(0.25f) // Loads a 25% smaller preview first — smoother scroll

            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // Smart caching
            .skipMemoryCache(false) // Keep recent images in memory
            .dontAnimate() // Avoid fade-in lag during fast scrolls
            .into(holder.imageView)

        // Wishlist state
        // Wishlist state
        val isWished = Sharedpref.WishlistPrefs.getWishlistIds(holder.itemView.context)

        if (isWished.contains(product.productId)) {
            product.Wished = true
            updateWishlistIcon(holder.wishlist, holder.itemView.context, product.Wished)
        } else {
            product.Wished = false
            updateWishlistIcon(holder.wishlist, holder.itemView.context, product.Wished)
        }




        // Cart state
        val isInCart = Sharedpref.CartPref.getCartIdForProduct(
            holder.itemView.context,
            product.productId
        ) != null
        Log.d("getting", "onBindViewHolder: $isInCart")
        if (isInCart) {
            holder.addBtn.visibility = View.GONE
            holder.qtyControls.visibility = View.VISIBLE
            holder.productQty.text = (cartQuantities[product.productId] ?: 1).toString()
        } else {
            holder.addBtn.visibility = View.VISIBLE
            holder.qtyControls.visibility = View.GONE
        }
        val quantityCount = Sharedpref.CartPref.getCartQuantities(context)
        Log.d("gettingquantity", "onBindViewHolder: $quantityCount")

        for ((productId, qty) in quantityCount) {
            ProductQuantity[productId] = qty

            if (product.productId == productId) {
                holder.productQty.text = qty.toString()

            }
        }

    }

    private fun updateWishlistIcon(
        button: ImageButton,
        context: android.content.Context,
        wished: Boolean
    ) {
        if (wished) {
            button.setImageResource(R.drawable.addedwishlist)
            button.setColorFilter(
                ContextCompat.getColor(context, R.color.red),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            button.setImageResource(R.drawable.fav)
            button.clearColorFilter()
        }
    }

    // Helper: updates quantity, persists to SharedPref, updates server, updates in-memory cache and UI
    private fun updateQuantity(
        context: android.content.Context,
        product: FeaturedProduct,
        newQty: Int
    ) {
        val customerId = SessionManager.getCustomerId(context) ?: ""
        val sessionId = SessionManager.getSessionId(context) ?: ""
        val cartId = Sharedpref.CartPref.getCartIdForProduct(context, product.productId)

        cartQuantities[product.productId] = newQty
        Sharedpref.CartPref.saveCartQuantities(context, mapOf(product.productId to newQty))

        if (cartId != null) {
            viewModel.updateCartQuantity(customerId, sessionId, cartId, newQty)
        } else {
            Log.e("CartUpdate", "Missing cartId for ${product.productId}")
        }

        // only update the item's quantity on screen (partial update)
        val idx = products.indexOf(product)
        if (idx >= 0) notifyItemChanged(idx, "quantityChanged")
    }

    override fun getItemCount(): Int = products.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newProducts: List<FeaturedProduct>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(
        newProducts: List<FeaturedProduct>,
        cartVM: fetchCartVM?,
        wishlistVM: WishListVM?
    ) {
        products.clear()
        products.addAll(newProducts)
        cartVM?.let { viewModel = it }
        wishlistVM?.let { wishViewModel = it }
        notifyDataSetChanged()
    }
}
