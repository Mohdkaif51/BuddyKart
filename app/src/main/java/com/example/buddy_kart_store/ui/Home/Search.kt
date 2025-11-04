package com.example.buddy_kart_store.ui.Home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.buddy_kart_store.databinding.ActivitySearchBinding
import com.example.buddy_kart_store.model.repository.CartRepo
import com.example.buddy_kart_store.model.repository.FetchWishListRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.model.retrofit_setup.login.SearchProduct
import com.example.buddy_kart_store.ui.drawer_section.MyWishlist
import com.example.buddy_kart_store.ui.recyclerviews.SearchTrendingProduct
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.GenericViewModelFactory
import com.example.buddy_kart_store.utlis.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Search : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    private val productList = mutableListOf<SearchProduct>()
    private lateinit var adapter: SearchTrendingProduct

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private lateinit var viewModel: WishListVM
    private lateinit var cartVM: fetchCartVM

    private val debounceDelay = 500L  // 500ms debounce

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val cartRepository = CartRepo(RetrofitClient.iInstance, this)
        val cartFactory = GenericViewModelFactory { fetchCartVM(cartRepository) }
        cartVM = ViewModelProvider(this, cartFactory)[fetchCartVM::class.java]

        binding.cartt.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }




        binding.progressbar.visibility = View.GONE
        val repository = FetchWishListRepo(RetrofitClient.iInstance)
        val factory = GenericViewModelFactory { WishListVM(repository) }
        viewModel = ViewModelProvider(this, factory)[WishListVM::class.java]
        setupRecyclerView()
        setupSearchView()
        setupNavigationButtons()
        binding.wishlist.setOnClickListener {
            val intent = Intent(this, MyWishlist::class.java)
            intent.putExtra("fromActivity", "PDP")
            startActivity(intent)
        }


        cartVM.cartCountLiveData.observe(this) { count ->
            if (count > 0) {
                binding.cartCount.visibility = View.VISIBLE
                val wishcount = count.toString()
                binding.cartCount.text = wishcount

            } else {
                binding.cartCount.visibility = View.GONE
            }
            val initialCartCount = Sharedpref.CartPrefs.getCartIds(this).size
            cartVM.cartCountLiveData.postValue(initialCartCount)

            binding.cart.setOnClickListener {
                val productId = "1234" // Example product ID
                cartVM.addProductToCart(productId, this)
            }
        }

        viewModel.wishCountLiveData.observe(this) { count ->
            if (count > 0) {
                binding.wishlistCount.visibility = View.VISIBLE
                val wishcount = count.toString()
                Log.d("gettingwishcount", "onCreate: $wishcount")
                binding.wishlistCount.text = wishcount

            } else {
                binding.wishlistCount.visibility = View.GONE
            }
            val initialWishCount = Sharedpref.WishlistPrefs.getWishlistIds(this).size
            viewModel.wishCountLiveData.postValue(initialWishCount)


        }
        val sessionid = SessionManager.getSessionId(context = this).toString()
        Log.d("sessionid", sessionid)


    }

    private fun setupRecyclerView() {
        adapter = SearchTrendingProduct(
            productList,
            viewModel,
            cartVM
        )
        binding.trendingRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.trendingRecyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { fetchSearchResults(it)  }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { handler.removeCallbacks(it) } // remove previous call
                searchRunnable = Runnable {
                    val query = newText?.trim()
                    if (!query.isNullOrEmpty()) {
                        fetchSearchResults(query)
                    } else {
                        productList.clear()
                        adapter.notifyDataSetChanged()
                        binding.noProductText.visibility = View.GONE
                        binding.trendingRecyclerView.visibility = View.GONE
                    }
                }
                handler.postDelayed(searchRunnable!!, debounceDelay)
                return true
            }
        })
    }

    private fun setupNavigationButtons() {
        binding.backButton.setOnClickListener { onBackPressed() }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun fetchSearchResults(query: String) {
        binding.progressbar.visibility = View.VISIBLE


            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = RetrofitClient.iInstance.searchProducts(
                        route = "wbapi/searchquery.searchproduct",
                        search = query
                    )

                    withContext(Dispatchers.Main) {
                        binding.progressbar.visibility = View.GONE
                    }

                    if (response.isSuccessful && response.body() != null) {
                        val rawResponse = response.body()!!.string()
                        Log.d("API_SEARCH", "onResponse: $rawResponse")

                        val json = JSONObject(rawResponse)
                        if (json.optString("success") == "true") {
                            val dataArray = json.getJSONArray("data")
                            productList.clear()

                            for (i in 0 until dataArray.length()) {
                                val obj = dataArray.getJSONObject(i)
                                val id = obj.optString("product_id")
                                val rawName = obj.optString("name", "N/A")
                                val name = rawName.replace(Regex("[^A-Za-z\\s]"), "").trim()
                                val price = String.format("%.2f", obj.optString("price", "0").toDouble())
                                val discount = obj.optString("discount")
                                val image = obj.optString("image")
                                val baseUrl = "https://hellobuddy.jkopticals.com/image/"

// Decode HTML entities (&amp; -> &)
                                val cleanPath = image
                                    .replace("&amp;", "&")
                                    .replace(" ", "%20") // Encode spaces for safety

                                val fullImageUrl =
                                    if (cleanPath.startsWith("http")) cleanPath else baseUrl + cleanPath

                                val special = obj.getString("special")

                                productList.add(
                                    SearchProduct(
                                        productId = id,
                                        name = name,
                                        imageUrl = fullImageUrl,
                                        price = price,
                                        rating = discount,
                                        favorite = false,
                                        special = special
                                    )
                                )
                            }

                            withContext(Dispatchers.Main) {
                                if (productList.isEmpty()) {
                                    binding.noProductText.visibility = View.VISIBLE
                                    binding.trendingRecyclerView.visibility = View.GONE
                                } else {
                                    binding.noProductText.visibility = View.GONE
                                    binding.trendingRecyclerView.visibility = View.VISIBLE
                                    adapter.notifyDataSetChanged()
                                }
                            }

                        } else {
                            withContext(Dispatchers.Main) {
                                productList.clear()
                                adapter.notifyDataSetChanged()
                                binding.noProductText.visibility = View.VISIBLE
                                binding.trendingRecyclerView.visibility = View.GONE
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@Search, "Server error", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("API_ERROR", e.message.toString())
                    withContext(Dispatchers.Main) {
                        binding.progressbar.visibility = View.GONE
                        Toast.makeText(this@Search, "Network error", Toast.LENGTH_SHORT).show()
                    }
                }

        }

    }
    override fun onResume() {
        super.onResume()
        val query = binding.searchView.query.toString().trim()
        if (query.isNotEmpty()) {
            fetchSearchResults(query)
        }
    }
}
