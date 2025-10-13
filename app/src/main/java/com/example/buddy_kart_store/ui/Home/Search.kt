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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.buddy_kart_store.databinding.ActivitySearchBinding
import com.example.buddy_kart_store.model.repository.FetchWishListRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.model.retrofit_setup.login.SearchProduct
import com.example.buddy_kart_store.ui.drawer_section.MyWishlist
import com.example.buddy_kart_store.ui.recyclerviews.SearchTrendingProduct
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.utlis.GenericViewModelFactory
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
    private val debounceDelay = 500L  // 500ms debounce

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.progressbar.visibility = View.GONE
        val repository = FetchWishListRepo(RetrofitClient.iInstance)
        val factory = GenericViewModelFactory { WishListVM(repository) }
        viewModel = ViewModelProvider(this, factory)[WishListVM::class.java]
        setupRecyclerView()
        setupSearchView()
        setupNavigationButtons()
        binding.wishlistbtn.setOnClickListener {
            val intent = Intent(this, MyWishlist::class.java)
            intent.putExtra("fromActivity", "PDP")
            startActivity(intent)
        }



    }

    private fun setupRecyclerView() {
        adapter = SearchTrendingProduct(productList  , viewModel)
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

    private fun fetchSearchResults(query: String) {
        binding.progressbar.visibility = View.VISIBLE

        RetrofitClient.iInstance.searchProducts(
            route = "wbapi/searchquery.searchproduct",
            search = query
        ).enqueue(object : Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                binding.progressbar.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val rawResponse = response.body()!!.string()
                    Log.d("API_SEARCH", "onResponse: $rawResponse")
                    try {
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

                                val fullImageUrl =
                                    "https://hello.buddykartstore.com/image/$image"

                                Log.d("gettingimggg", "onResponse: $image")

                                productList.add(
                                    SearchProduct(
                                        productId = id,
                                        name = name,
                                        imageUrl = fullImageUrl,
                                        price = price,
                                        rating = discount,
                                        favorite = false
                                    )
                                )
                            }

                            if (productList.isEmpty()) {
                                binding.noProductText.visibility = View.VISIBLE
                                binding.trendingRecyclerView.visibility = View.GONE
                            } else {
                                binding.noProductText.visibility = View.GONE
                                binding.trendingRecyclerView.visibility = View.VISIBLE
                                adapter.notifyDataSetChanged()
                            }

                        } else {
                            productList.clear()
                            adapter.notifyDataSetChanged()
                            binding.noProductText.visibility = View.VISIBLE
                            binding.trendingRecyclerView.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@Search, "Parsing Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                binding.progressbar.visibility = View.GONE
                Log.e("API_ERROR", t.message.toString())
                Toast.makeText(this@Search, "API Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
