package com.example.buddy_kart_store.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddy_kart_store.model.repository.FetchOrderRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.order

class fetchOrderVM(private val fetchOrderRepo: FetchOrderRepo) : ViewModel() {

    private val _orders = MutableLiveData<List<order>>()
    val orders: LiveData<List<order>> get() = _orders

    fun fetchOrder(customerId: String) {
        fetchOrderRepo.fetchOrder(customerId) { orderList ->
            _orders.postValue(orderList)
        }
    }
}
