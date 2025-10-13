package com.example.buddy_kart_store.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddy_kart_store.model.repository.CartRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.CartDetail

class fetchCartVM(private val repository: CartRepo) : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartDetail>>()
    val cartItems: LiveData<List<CartDetail>> get() = _cartItems

//    for listing of  cart product
    fun fetchCart(customerId: String, sessionId: String) {
        repository.fetchCart(customerId, sessionId).observeForever { items ->
            _cartItems.postValue(items) // ✅ now you pass the result
        }
    }

    fun addToCart(customerId: String, sessionId: String, productId: String){
        repository.addToCart(
            customerId,
            sessionId,
            productId
        )
    }

}

