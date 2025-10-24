package com.example.buddy_kart_store.ui.recyclerviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.TopCategory
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utlis.HomeModule

class HomeAdapter(
    private var modules: List<HomeModule>,
    private val cartVM: fetchCartVM,
    private val wishlistVM: WishListVM
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_BANNER = 0
        private const val TYPE_CATEGORY = 1
        private const val TYPE_PRODUCTS = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (modules[position]) {
            is HomeModule.Banner -> TYPE_BANNER
            is HomeModule.Category -> TYPE_CATEGORY
            is HomeModule.Products -> TYPE_PRODUCTS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_BANNER -> BannerViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_banner_slider, parent, false)
            )
            TYPE_CATEGORY -> CategoryViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_category_list, parent, false)
            )
            else -> ProductViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_product_list, parent, false)
            )
        }
    }

    override fun getItemCount() = modules.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val module = modules[position]) {
            is HomeModule.Banner -> (holder as BannerViewHolder).bind(module.images)
            is HomeModule.Category -> (holder as CategoryViewHolder).bind(module.categories as MutableList<TopCategory>)
            is HomeModule.Products -> (holder as ProductViewHolder).bind(module.products, cartVM, wishlistVM)
        }

    }

    fun updateList(newModules: List<HomeModule>) {
        modules = newModules
        notifyDataSetChanged()
    }
}

