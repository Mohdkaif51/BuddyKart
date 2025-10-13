package com.example.buddy_kart_store.ui.drawer_section

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buddy_kart_store.databinding.ActivityOrderPageBinding
import com.example.buddy_kart_store.model.repository.FetchOrderRepo
import com.example.buddy_kart_store.model.repository.FetchWishListRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.OrderList
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.model.retrofit_setup.login.order
import com.example.buddy_kart_store.ui.Home.MainActivity
import com.example.buddy_kart_store.ui.recyclerviews.OrderListingRecycler
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.fetchOrderVM
import com.example.buddy_kart_store.utlis.GenericViewModelFactory
import com.example.buddy_kart_store.utlis.SessionManager
import kotlin.jvm.java

class OrderPage : AppCompatActivity() {
    private lateinit var binding: ActivityOrderPageBinding
    private lateinit var adapter: OrderListingRecycler
    private lateinit var viewModel: fetchOrderVM

    private val orderList = mutableListOf<order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.profilebackButton.setOnClickListener {
            navigateBackToMainWithDrawerOpen()
        }

        adapter = OrderListingRecycler(orderList) { selectedOrder ->
            // Open OrderDetailActivity
            val intent = Intent(this, OrderDetail::class.java)
            intent.putExtra("ORDER_ID", selectedOrder.orderId) // Pass ID
            startActivity(intent)
        }
        setUpRecyclerView()

        // ✅ Correct Repository and ViewModel
        val repository = FetchOrderRepo(RetrofitClient.iInstance)
        val factory = GenericViewModelFactory { fetchOrderVM(repository) }
        viewModel = ViewModelProvider(this, factory)[fetchOrderVM::class.java]

        // ✅ Observe LiveData
        viewModel.orders.observe(this) { orders ->
            orderList.clear()
            orderList.addAll(orders)
            adapter.notifyDataSetChanged()
        }

        val customerId = SessionManager.getCustomerId(this) ?: ""
        viewModel.fetchOrder(customerId)
    }

    private fun navigateBackToMainWithDrawerOpen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("OPEN_DRAWER", true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter
    }
}
