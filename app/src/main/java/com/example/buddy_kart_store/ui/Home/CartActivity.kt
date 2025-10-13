package com.example.buddy_kart_store.ui.Home

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buddy_kart_store.databinding.ActivityCartBinding
import com.example.buddy_kart_store.model.repository.CartRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.CartDetail
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.ui.recyclerviews.CartRecyclerView
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utlis.GenericViewModelFactory
import com.example.buddy_kart_store.utlis.SessionManager

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartRecyclerView
    private val cartList = mutableListOf<CartDetail>()
    private lateinit var viewModel: fetchCartVM


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener {
            onBackPressed()

        }
        cartAdapter = CartRecyclerView(cartList, object : CartRecyclerView.CartItemListener {
            override fun onQuantityChanged(cartItem: CartDetail, newQuantity: Int) {
                // Call your ViewModel to update cart quantity on server
//                viewModel.updateCartQuantity(cartItem.cart_id, newQuantity)
            }

            override fun onItemDeleted(cartItem: CartDetail) {
                // Call your ViewModel to delete the item from server cart
//                viewModel.deleteCartItem(cartItem.cart_id)
            }
        })
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this , LinearLayoutManager.VERTICAL, false)

        binding.cartRecyclerView.adapter = cartAdapter


        val repository = CartRepo(RetrofitClient.iInstance)
        val factory = GenericViewModelFactory { fetchCartVM(repository) }
        viewModel = ViewModelProvider(this, factory)[fetchCartVM::class.java]


        val sessionid = SessionManager.getSessionId(this).toString()
        val customerId = SessionManager.getCustomerId(this).toString()
        Log.d("sessionid", "onCreate: $sessionid")

        viewModel.fetchCart(customerId, sessionid  , )

        viewModel.cartItems.observe(this) { items ->
            cartList.clear()
            cartList.addAll(items)
            cartAdapter.notifyDataSetChanged()
        }


    }

}
