package com.example.buddy_kart_store.ui.recyclerviews

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.CartDetail
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.SessionManager

class CartRecyclerView(
    private val cartList: MutableList<CartDetail>,
    private val listener: CartItemListener,
    private val viewModel: fetchCartVM
) : RecyclerView.Adapter<CartRecyclerView.ViewHolder>() {
//private lateinit var Sharedpref: Sharedpref

    interface CartItemListener {
        //        fun onQuantityChanged(cartItem: CartDetail, newQuantity: Int , cart_id: String , product_id: String)
        fun onItemDeleted(cartItem: CartDetail)

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.imageView2)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.price)
        val productQuantity: TextView = itemView.findViewById(R.id.qtyText)
        val deleteButton: TextView = itemView.findViewById(R.id.removeButton)
        val decreaseQty: ImageView = itemView.findViewById(R.id.decreaseQty)
        val increaseQty: ImageView = itemView.findViewById(R.id.increaseQty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_card, parent, false)
        return ViewHolder(view)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sessionid = SessionManager.getSessionId(context = holder.itemView.context).toString()
        val customerId = SessionManager.getCustomerId(context = holder.itemView.context).toString()
        Log.d("gettingsessionid", "onBindViewHolder: $sessionid")
        val sharedPref = Sharedpref(holder.itemView.context)

        val product = cartList[position]
        holder.productName.text = product.name
        holder.productPrice.text = "₹${product.price}"
        holder.productQuantity.text = product.quantity.toString()

        // Load image
        Glide.with(holder.itemView.context)
            .load(product.image)
            .into(holder.productImage)

        sharedPref.saveCartId(product.cart_id)

        holder.deleteButton.setOnClickListener {
            val context = holder.itemView.context


            AlertDialog.Builder(context)
                .setTitle("Remove Item")
                .setMessage("Are you sure you want to remove this item from your cart?")
                .setPositiveButton("Yes") { dialog, _ ->
                    // ✅ Call ViewModel to delete item
                    viewModel.deleteCartItem(
                        customerId,
                        sessionid,
                        product.product_id,
                        product.cart_id,
                        // ✅ only cart_id is needed for API
                    )
                    Sharedpref.CartPref.clearCart(context )


                    // ✅ Update UI instantly
                    cartList.removeAt(position)

                    viewModel.removeProductFromCart(product.product_id, context)
                    Sharedpref.CartPref.deleteProduct(context, product.product_id)


                    notifyItemRemoved(position)

                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()

        }


        // Decrease quantity
        holder.decreaseQty.setOnClickListener {
            if (product.quantity > 1) {
                val newQuantity = product.quantity - 1

                // Update UI immediately
                product.quantity = newQuantity
                holder.productQuantity.text = newQuantity.toString()

                // Call API to update quantity
                viewModel.onQuantityChanged(customerId, sessionid, product.cart_id, newQuantity)
            }
        }

        holder.increaseQty.setOnClickListener {
            val newQuantity = product.quantity + 1

            // Update UI immediately
            product.quantity = newQuantity
            holder.productQuantity.text = newQuantity.toString()

            viewModel.onQuantityChanged(customerId, sessionid, product.cart_id, newQuantity)
        }


    }

    override fun getItemCount(): Int = cartList.size
}
