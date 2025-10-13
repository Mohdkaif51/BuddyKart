package com.example.buddy_kart_store.ui.drawer_section

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.buddy_kart_store.databinding.ActivityMyWishlistBinding
import com.example.buddy_kart_store.model.repository.FetchWishListRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.model.retrofit_setup.login.TrendingProduct
import com.example.buddy_kart_store.ui.Home.MainActivity
import com.example.buddy_kart_store.ui.recyclerviews.WishlistRecycler
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.utlis.GenericViewModelFactory
import com.example.buddy_kart_store.utlis.SessionManager

class MyWishlist : AppCompatActivity() {

    private lateinit var binding: ActivityMyWishlistBinding
    private lateinit var viewModel: WishListVM
    private lateinit var adapter: WishlistRecycler
    private var productList: MutableList<TrendingProduct> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = FetchWishListRepo(RetrofitClient.iInstance)
        val factory = GenericViewModelFactory { WishListVM(repository) }
        viewModel = ViewModelProvider(this, factory)[WishListVM::class.java]

        val customerId = SessionManager.getCustomerId(this).toString()

        adapter = WishlistRecycler(productList, object : WishlistRecycler.WishlistListener {
            override fun addToWishlist(productId: String, position: Int, onResult: (Boolean) -> Unit) {
                viewModel.addToWishlist(productId, customerId) { success, message ->
                    onResult(success)
                    if (!success) Toast.makeText(this@MyWishlist, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun removeFromWishlist(productId: String, position: Int, onResult: (Boolean) -> Unit) {
                viewModel.removeFromWishlist(productId, customerId) { success, message ->
                    onResult(success)
                    if (!success) Toast.makeText(this@MyWishlist, message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        binding.wishlistrecycler.layoutManager = GridLayoutManager(this, 2)
        binding.wishlistrecycler.adapter = adapter

        // Observe LiveData
        viewModel.wishlist.observe(this) { data ->
            if (data != null) adapter.updateData(data.toMutableList())
        }

        // Fetch wishlist
        viewModel.fetchWishList(customerId) { success, message, data ->
            if (success && data != null) viewModel.setWishlist(data)
            else Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        binding.back.setOnClickListener { onBackPressed() }
    }

    private fun navigateBackToMainWithDrawerOpen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("OPEN_DRAWER", true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        when (intent.getStringExtra("fromActivity")) {
            "PDP" -> finish() // back to PDP
            "CategoryPage" -> finish() // back to CategoryPage
            "Search" -> finish() // back to Search
            else -> navigateBackToMainWithDrawerOpen() // default: main drawer
        }
    }
}
