package com.example.buddy_kart_store.utlis

import com.example.buddy_kart_store.model.retrofit_setup.login.FeaturedProduct
import com.example.buddy_kart_store.model.retrofit_setup.login.TopCategory

data class BannerItem(val title: String, val image: String, val link: String)
data class FeaturedProduct(
    val id: String,
    val name: String,
    val thumb: String,
    val price: String,
    val rating: Int,
    val href: String
)
data class TopCategory(
    val id: String,
    val name: String,
    val image: String,
    val href: String
)

sealed class HomeModule {
    data class Banner(val images: MutableList<BannerItem>, val interval: Long) : HomeModule()
    data class Category(val categories: List<TopCategory>) : HomeModule()
    data class Products(val products: List<FeaturedProduct>, val title: String) : HomeModule()
}
