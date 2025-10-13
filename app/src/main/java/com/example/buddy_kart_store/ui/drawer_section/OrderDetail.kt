package com.example.buddy_kart_store.ui.drawer_section

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buddy_kart_store.databinding.ActivityOrderDetailBinding
import com.example.buddy_kart_store.model.repository.OrderSummeryRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.ui.recyclerviews.OrderSummeryRecycler
import com.example.buddy_kart_store.ui.viewmodel.OrderSummeryVm
import com.example.buddy_kart_store.utlis.GenericViewModelFactory
import com.example.buddy_kart_store.utlis.SessionManager

class OrderDetail : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var viewModel: OrderSummeryVm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderId = intent.getStringExtra("ORDER_ID") ?: ""
        val customerId = SessionManager.getCustomerId(this) ?: ""

        Log.d("OrderDetail", "onCreate: orderId = $orderId, customerId = $customerId")

        // Setup ViewModel
        val repository = OrderSummeryRepo(RetrofitClient.iInstance)
        val factory = GenericViewModelFactory { OrderSummeryVm(repository) }
        viewModel = ViewModelProvider(this, factory)[OrderSummeryVm::class.java]

        // Back button
        binding.back.setOnClickListener { onBackPressed() }

        // Setup RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        // Observe LiveData
        viewModel.orderSummery.observe(this) { orderDetail ->
            orderDetail?.let { detail ->

                //  Set RecyclerView adapter for products
                val adapter = OrderSummeryRecycler(detail.orderSummary.items)
                binding.recyclerView.adapter = adapter

                // Populate order summary fields
                binding.subtotal.text = detail.orderSummary.subTotal
                binding.delivery.text = detail.orderSummary.deliveryCharge
//                binding.discount.text = detail.orderSummary.flatshippingrate
                binding.discountextra.text = detail.orderSummary.ecotax
                binding.discountextraextra.text = detail.orderSummary.vat
                binding.total.text = detail.orderSummary.grandTotal

                // 3️⃣ Populate order details fields
                binding.orderid.text = detail.orderDetails.orderId
                binding.txtPaymentMethod.text = detail.orderDetails.paymentMethod
                binding.address.text = detail.orderDetails.deliveryAddress
                binding.orderplaced.text = detail.orderDetails.orderPlaced

            }
        }

        // Fetch order details from API
        viewModel.fetchOrderDetails(orderId, customerId)
    }
}
