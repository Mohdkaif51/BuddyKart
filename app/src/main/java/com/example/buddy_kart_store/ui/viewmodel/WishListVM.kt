package com.example.buddy_kart_store.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddy_kart_store.model.repository.FetchWishListRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.TrendingProduct
import com.example.buddy_kart_store.utils.Sharedpref

class WishListVM(private val fetchWishListRepo: FetchWishListRepo) : ViewModel() {

    private val _wishlist = MutableLiveData<List<TrendingProduct>>()
    val wishlist: LiveData<List<TrendingProduct>> get() = _wishlist

    fun setWishlist(data: List<TrendingProduct>) {
        _wishlist.value = data
    }

    fun fetchWishList(
        customerId: String,
        onResult: (Boolean, String, List<TrendingProduct>?, Int) -> Unit
    ) {
        fetchWishListRepo.fetchWishList(customerId) { success, message, data, count ->
            if (success && data != null) {
                _wishlist.value = data
                wishCountLiveData.postValue(count)
            }
            onResult(success, message, data, count)
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




    val wishCountLiveData = MutableLiveData<Int>().apply { value = 0 }

    // Call this after fetching cart from API
    fun updateWishCount(cartIds: Set<String>) {
        wishCountLiveData.postValue(cartIds.size)
    }

    // For demonstration: add/remove product
    fun addProductToWishList(productId: String, context: Context) {
        Sharedpref.CartPrefs.addProductId(context, productId)
        val currentCount = wishCountLiveData.value ?: 0
        wishCountLiveData.postValue(currentCount + 1)
    }
    fun refreshWishCount(context: Context) {
        val count = Sharedpref.WishlistPrefs.getWishlistIds(context).size
        wishCountLiveData.postValue(count)
    }

// Call refreshWishCount(context) whenever you add/remove product locally
fun syncWishlistWithServer(context: Context, serverList: List<TrendingProduct>) {
    Sharedpref.WishlistPrefs.clearWishlist(context)
    serverList.forEach { Sharedpref.WishlistPrefs.addProductId(context, it.product_id) }
    refreshWishCount(context)
}

    val wishlistUpdated = MutableLiveData<Boolean>()

    fun notifyWishlistChanged() {
        wishlistUpdated.postValue(true)
    }


}
