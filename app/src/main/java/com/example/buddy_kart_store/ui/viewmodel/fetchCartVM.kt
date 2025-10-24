package com.example.buddy_kart_store.ui.viewmodel


import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddy_kart_store.model.repository.CartRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.BillingAmt
import com.example.buddy_kart_store.model.retrofit_setup.login.CartDetail
import com.example.buddy_kart_store.utils.Sharedpref

class fetchCartVM(private val repository: CartRepo) : ViewModel() {
    private val _cartQuantities = MutableLiveData<Map<String, Int>>(emptyMap())
    val cartQuantitiesLiveData: LiveData<Map<String, Int>> get() = _cartQuantities

    private val _cartItems = MutableLiveData<List<CartDetail>>()
    val cartItems: LiveData<List<CartDetail>> get() = _cartItems
//    val cartCountLiveData = MutableLiveData<Int>() // <-- add this

    private val _billingAmt = MutableLiveData<BillingAmt>()
    val billingAmt: LiveData<BillingAmt> get() = _billingAmt


    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchCart(customerId: String, sessionId: String) {
        _isLoading.postValue(true) // Set loading state to true

        val (cartLiveData, billingLiveData) = repository.fetchCart(customerId, sessionId)

        // Observe cart items
        cartLiveData.observeForever { items ->
            _isLoading.postValue(false)
            _cartItems.postValue(items)

            // Build quantity map from the items just received
            val quantityMap = items.associate { it.quantity to it.quantity }
            _cartQuantities.postValue(quantityMap as Map<String, Int>)
        }

        // Observe billing info
        billingLiveData.observeForever { billing ->
            _billingAmt.postValue(billing)
        }
    }

    fun addToCart(
        customerId: String,
        sessionId: String,
        productId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        repository.addToCart(customerId, sessionId, productId) {
                success,
                message,
            ->
            if (success) {
                Log.d("CartVM", "Item added successfully: $message")
                fetchCart(customerId, sessionId) // Refresh cart

            } else {
                Log.e("CartVM", "Add failed: $message")
            }

            // ✅ Forward result to adapter
            onResult(success, message)
        }
    }

    fun deleteCartItem(
        customerId: String, sessionId: String, productId: String, cart_id: String
    ) {
        repository.deleteProduct(customerId, sessionId, productId, cart_id) { success, message ->
            if (success) {
                Log.d("CartVM", "Item deleted successfully: $message")
                fetchCart(customerId, sessionId) // ✅ refresh cart after deletion
            } else {
                Log.e("CartVM", "Delete failed: $message")
            }
        }


    }


    fun deleteCart(customerId: String, sessionId: String) {
        repository.deleteCart(customerId, sessionId) { success, message ->
            if (success) {
                Log.d("CartVM", "Cart deleted successfully: $message")

                // Clear cart items in UI
                _cartItems.postValue(emptyList())

                // Instead of setting billing to 0 here, call fetchCart()
                // fetchCart() will update the billing info automatically
                fetchCart(customerId, sessionId)
            } else {
                Log.e("CartVM", "Delete failed: $message")
            }
        }
    }

    fun onQuantityChanged(customerId: String, sessionId: String, cartId: String, newQuantity: Int) {
        updateCartQuantity(customerId, sessionId, cartId, newQuantity)
    }

//    val cartQuantitiesLiveData = MutableLiveData<Map<String, Int>>(emptyMap())

    fun updateCartQuantity(
        customerId: String, sessionId: String, cartId: String, newQuantity: Int
    ) {
        repository.updateQuantity(
            customerId, sessionId, cartId, newQuantity.toString()
        ) { success, message ->
            if (success) {
                Log.d("CartVM", "Quantity updated successfully: $message")
                fetchCart(customerId, sessionId) // Refresh cart

            } else {
                Log.e("CartVM", "Quantity update failed: $message")
            }
        }
    }


    val cartCountLiveData = MutableLiveData<Int>().apply { value = 0 }

    // Call this after fetching cart from API
    fun updateCartCount(cartIds: Set<String>) {
        cartCountLiveData.postValue(cartIds.size)
    }

    // For demonstration: add/remove product
    fun addProductToCart(productId: String, context: Context) {
        Sharedpref.CartPrefs.addProductId(context, productId)
        val currentCount = cartCountLiveData.value ?: 0
        cartCountLiveData.postValue(currentCount + 1)
    }

    fun removeProductFromCart(productId: String, context: Context) {
        Sharedpref.CartPrefs.removeProductId(context, productId)
        val currentCount = cartCountLiveData.value ?: 1
        cartCountLiveData.postValue((currentCount - 1).coerceAtLeast(0))
    }


}