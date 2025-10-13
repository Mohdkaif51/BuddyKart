package com.example.buddy_kart_store.ui.Home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buddy_kart_store.databinding.ActivityCategorypageBinding
import com.example.buddy_kart_store.model.retrofit_setup.login.CategoryProduct
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.model.retrofit_setup.login.categories
import com.example.buddy_kart_store.ui.drawer_section.MyWishlist
import com.example.buddy_kart_store.ui.recyclerviews.LeftCategory
import com.example.buddy_kart_store.ui.recyclerviews.RIghtCategoryProduct
import com.example.buddy_kart_store.ui.recyclerviews.SubCategoryAdapter
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response




class Categorypage : AppCompatActivity() {

    companion object {
        private const val EXTRA_PRODUCT_ID = "id"

        fun launch(context: Context, id: Int) {
            val intent = Intent(context, Categorypage::class.java)
            intent.putExtra(EXTRA_PRODUCT_ID, id)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityCategorypageBinding
    private var categories = mutableListOf<categories>()
    private var categoryProducts = mutableListOf<CategoryProduct>()
    private var subCategories = mutableListOf<categories>()

    private lateinit var categoryAdapter: LeftCategory
    private lateinit var subCategoryAdapter: SubCategoryAdapter
    private lateinit var categoryProductAdapter: RIghtCategoryProduct

    private var categoryId: Int = 0
    private var categoryName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategorypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryId = intent.getIntExtra("id", 0)
        categoryName = intent.getStringExtra("category_name")

        Log.d("gettingcategoryid", "onCreate: $categoryId")

        supportActionBar?.title = categoryName ?: "Category"

        setupAdapters()
        setupClicks()
        fetchCategories(categoryId)
    }

    private fun setupAdapters() {
        categoryAdapter = LeftCategory(categories) { selectedCategory ->
            openCategory(selectedCategory)
        }
        binding.ItemCategory.layoutManager = LinearLayoutManager(this)
        binding.ItemCategory.adapter = categoryAdapter

        subCategoryAdapter = SubCategoryAdapter(subCategories) { selectedSubCat ->
            fetchCategoryProducts(selectedSubCat.id)
        }
        binding.subCategory.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.subCategory.adapter = subCategoryAdapter

        categoryProductAdapter = RIghtCategoryProduct(categoryProducts)
        binding.productList.layoutManager = GridLayoutManager(this, 2)
        binding.productList.adapter = categoryProductAdapter
    }

    private fun setupClicks() {
        binding.backbtn.setOnClickListener { onBackPressed() }
        binding.searchbtn.setOnClickListener { startActivity(Intent(this, Search::class.java)) }
        binding.favbtn.setOnClickListener {
            startActivity(Intent(this, MyWishlist::class.java))
        }
        binding.cartbtn.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun fetchCategories(categoryId: Int) {
        RetrofitClient.iInstance.getCategory(
            route = "wbapi/wishingapi.getcategory",
            category_id = categoryId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val raw = response.body()!!.string()
                    val jsonObject = JSONObject(raw)
                    val dataArray = jsonObject.optJSONArray("categories") ?: JSONArray()

                    categories.clear()
                    for (i in 0 until dataArray.length()) {
                        categories.add(parseCategory(dataArray.getJSONObject(i)))
                    }

                    categoryAdapter.notifyDataSetChanged()

                    // ✅ Select the category passed via intent
                    val selectedCategory = categories.find { it.id == categoryId.toString() } ?: categories[0]
                    openCategory(selectedCategory)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Categorypage", "API Failure: ${t.message}")
            }
        })
    }

    private fun openCategory(category: categories) {
        // Left category selection
        categoryAdapter.setSelectedCategory(categories.indexOf(category))

        // Subcategories
        subCategories.clear()
        subCategories.addAll(category.children)
        subCategoryAdapter.notifyDataSetChanged()

        // Fetch products
        if (category.children.isNotEmpty()) {
            val firstSub = category.children[0]
            subCategoryAdapter.setSelectedPosition(0)
            fetchCategoryProducts(firstSub.id)
        } else {
            fetchCategoryProducts(category.id)
        }
    }

    private fun parseCategory(jsonObject: JSONObject): categories {
        val name = jsonObject.optString("name")
        val id = jsonObject.optInt("category_id")  // Keep as Int
        val image = jsonObject.optString("image")
        val childrenList = mutableListOf<categories>()
        val childrenArray = jsonObject.optJSONArray("children")
        if (childrenArray != null) {
            for (i in 0 until childrenArray.length()) {
                childrenList.add(parseCategory(childrenArray.getJSONObject(i)))
            }
        }
        return categories(name, id.toString(), image, childrenList)
    }

    private fun fetchCategoryProducts(categoryId: String) {
        RetrofitClient.iInstance.getCategoryProduct(
            route = "wbapi/productapi.getproduct",
            category_id = categoryId.toString()
        ).enqueue(object : Callback<ResponseBody> {
            @SuppressLint("DefaultLocale", "NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                categoryProducts.clear()
                if (response.isSuccessful && response.body() != null) {
                    val raw = response.body()!!.string()
                    val jsonObject = JSONObject(raw)
                    val dataArray = jsonObject.optJSONArray("data") ?: JSONArray()

                    for (i in 0 until dataArray.length()) {
                        val obj = dataArray.getJSONObject(i)
                        val imgurl = obj.optString("image")
                        val baseUrl = "https://hello.buddykartstore.com/image"
                        val imageUrl = if (imgurl.startsWith("http")) imgurl else "$baseUrl/$imgurl"

                        categoryProducts.add(
                            CategoryProduct(
                                name = obj.optString("name"),
                                imageUrl = imageUrl,
                                productId = obj.optInt("product_id"),
                                price = String.format("%.2f", obj.optDouble("price")),
                                discount = obj.optString("discount")
                            )
                        )
                    }

                    categoryProductAdapter.notifyDataSetChanged()
                    binding.productList.visibility =
                        if (categoryProducts.isEmpty()) View.GONE else View.VISIBLE
                    binding.noProductsImage.visibility =
                        if (categoryProducts.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    binding.productList.visibility = View.GONE
                    binding.noProductsImage.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                binding.productList.visibility = View.GONE
                binding.noProductsImage.visibility = View.VISIBLE
            }
        })
    }
}
