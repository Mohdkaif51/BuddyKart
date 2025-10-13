package com.example.buddy_kart_store.ui.recyclerviews

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.FeaturedProduct
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utlis.HomeModule
import kotlin.collections.toMutableList

class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(products: List<FeaturedProduct>, cartVM: fetchCartVM, wishlistVM: WishListVM) {
        val recyclerView = itemView.findViewById<RecyclerView>(R.id.homeProductRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = HomeProductRecyclerView(products.toMutableList(), cartVM, wishlistVM)
    }
}

