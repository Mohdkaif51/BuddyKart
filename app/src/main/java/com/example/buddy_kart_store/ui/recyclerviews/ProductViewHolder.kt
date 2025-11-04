package com.example.buddy_kart_store.ui.recyclerviews

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.FeaturedProduct
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM

class ProductViewHolder(itemView: View, sharedPool: RecyclerView.RecycledViewPool) :
    RecyclerView.ViewHolder(itemView) {

    private val recyclerView: RecyclerView = itemView.findViewById(R.id.homeProductRecyclerView)
    private var adapter: HomeProductRecyclerView? = null

    init {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).apply {
                initialPrefetchItemCount = 4

            }
            setRecycledViewPool(sharedPool)
            setHasFixedSize(true)
            itemAnimator = null
            isNestedScrollingEnabled = false

        }
    }

    fun bind(products: List<FeaturedProduct>, cartVM: fetchCartVM, wishlistVM: WishListVM) {
        if (adapter == null) {
            adapter = HomeProductRecyclerView(products.toMutableList(), cartVM, wishlistVM)
            recyclerView.adapter = adapter
        } else {
            adapter?.updateData(products, cartVM, wishlistVM)
        }
    }
}
