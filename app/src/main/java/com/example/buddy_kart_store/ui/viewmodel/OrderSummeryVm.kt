package com.example.buddy_kart_store.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddy_kart_store.model.repository.OrderSummeryRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.OrderDetail

class OrderSummeryVm(private val repository: OrderSummeryRepo) : ViewModel() {

    private val _orderSummery = MutableLiveData<OrderDetail>()
    val orderSummery: MutableLiveData<OrderDetail> = _orderSummery


    fun fetchOrderDetails(orderId: String, customerId: String) {
        repository.fetchOrderDetails(orderId, customerId) { orderDetail ->
            _orderSummery.postValue(orderDetail )

        }

    }



}