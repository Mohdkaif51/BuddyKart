package com.example.buddy_kart_store.ui.recyclerviews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.buddy_kart_store.utlis.SessionManager

class RelatedProductAdapter(
    private var productList: List<RelatedProduct>,
    private val onItemClick: (RelatedProduct) -> Unit,
    private val wishViewModel: WishListVM
) : RecyclerView.Adapter<RelatedProductAdapter.RelatedViewHolder>() {

    inner class RelatedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.homeproductImage)
        val productName: TextView = itemView.findViewById(R.id.homeproductName)
        val productPrice: TextView = itemView.findViewById(R.id.homeproductdisPrice)
        val wishlist: ImageView = itemView.findViewById(R.id.wishlist)
        val productDesc: TextView = itemView.findViewById(R.id.desctxt)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelatedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.homepage_product, parent, false)
        return RelatedViewHolder(view)
    }

    override fun onBindViewHolder(holder: RelatedViewHolder, position: Int) {
        val product = productList[position]

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

    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<RelatedProduct>) {
        productList = newList
        notifyDataSetChanged()
    }
}
