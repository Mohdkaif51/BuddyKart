package com.example.buddy_kart_store.utils

import com.example.buddy_kart_store.model.retrofit_setup.login.CartItem

object CartManager {
    private val cartItems = mutableListOf<CartItem>()

    fun addToCart(item: CartItem) {
        cartItems.add(item)
    }

    fun getCartItems(): List<CartItem> = cartItems
}
