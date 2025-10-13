package com.example.buddy_kart_store.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddy_kart_store.model.repository.FetchWishListRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.TrendingProduct

class WishListVM(private val fetchWishListRepo: FetchWishListRepo) : ViewModel() {

    private val _wishlist = MutableLiveData<List<TrendingProduct>>()
    val wishlist: LiveData<List<TrendingProduct>> get() = _wishlist

    fun setWishlist(data: List<TrendingProduct>) {
        _wishlist.value = data
    }

    fun fetchWishList(customerId: String, onResult: (Boolean, String, List<TrendingProduct>?) -> Unit) {
        fetchWishListRepo.fetchWishList(customerId) { success, message, data ->
            if (success && data != null) _wishlist.value = data
            onResult(success, message, data)
        }
    }

    fun addToWishlist(productId: String, customerId: String, onResult: (Boolean, String) -> Unit) {
        fetchWishListRepo.addToWishlist(customerId, productId) { success, message ->
            if (success) {
                _wishlist.value = _wishlist.value?.map {
                    if (it.product_id == productId) it.copy(isWished = true) else it
                }
            }
            onResult(success, message)
        }
    }

    fun removeFromWishlist(productId: String, customerId: String, onResult: (Boolean, String) -> Unit) {
        fetchWishListRepo.removeFromWishlist(customerId, productId) { success, message ->
            if (success) {
                _wishlist.value = _wishlist.value?.filter { it.product_id != productId }
            }
            onResult(success, message)
        }
    }
}
