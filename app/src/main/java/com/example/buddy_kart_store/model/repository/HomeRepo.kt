package com.example.buddy_kart_store.model.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.buddy_kart_store.model.retrofit_setup.login.FeaturedProduct
import com.example.buddy_kart_store.model.retrofit_setup.login.TopCategory
import com.example.buddy_kart_store.model.retrofit_setup.login.apiService
import com.example.buddy_kart_store.utlis.BannerItem
import com.example.buddy_kart_store.utlis.HomeModule
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeRepo(private val apiService: apiService) {

    val homeModulesLiveData = MutableLiveData<List<HomeModule>>()

    fun fetchHomePage() {
        apiService.getHome().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {


                try {
                    val body = response.body()?.string() ?: return
                    val jsonArray = JSONArray(body)
                    Log.d("gettingjson", "onResponse: $jsonArray")
                    val modules = parseHomeJson(jsonArray)
                    homeModulesLiveData.postValue(modules)

                } catch (e: Exception) {
                    Log.e("HomeRepo", "Parse error: ${e.message}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("HomeRepo", "API Failure: ${t.message}")
            }
        })
    }

    private fun parseHomeJson(jsonArray: JSONArray): List<HomeModule> {
        val modules = mutableListOf<HomeModule>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            when (obj.getString("type")) {
                "banner" -> {
                    val banners = mutableListOf<BannerItem>()
                    val arr = obj.getJSONArray("banners")
                    for (j in 0 until arr.length()) {
                        val b = arr.getJSONObject(j)
                        val image = b.getString("image")
                        val thumb = "https://hello.buddykartstore.com/image/$image"
                        Log.d("gettingimagein", "parseHomeJson: $image")

                        banners.add(
                            BannerItem(
                                b.getString("title"),
                                thumb,
                                b.getString("link")
                            )
                        )
                    }
                    modules.add(HomeModule.Banner(banners, obj.optLong("interval", 5000)))
                }

                "category" -> {
                    val categories = mutableListOf<TopCategory>()
                    val arr = obj.getJSONArray("categories")
                    for (j in 0 until arr.length()) {
                        val c = arr.getJSONObject(j)
                        val raw = c.getString("name")
                        val name = raw.replace(Regex("[^A-Za-z\\s]"), "").trim()
                        categories.add(
                            TopCategory(
                                c.getString("category_id"),
                                name,
                                c.getString("image"),
                                c.getString("href")
                            )
                        )
                    }
                    modules.add(HomeModule.Category(categories))

                }

                "featured" -> {
                    val products = mutableListOf<FeaturedProduct>()
                    val arr = obj.getJSONArray("prods")
                    for (j in 0 until arr.length()) {
                        val p = arr.getJSONObject(j)
                        val raw = p.getString("name")
                        val name = raw.replace(Regex("[^A-Za-z\\s]"), "").trim()
                        products.add(
                            FeaturedProduct(
                                p.getString("product_id"),
                                name,
                                p.getString("thumb"),
                                p.getString("description"),
                                p.getString("price"),
                                p.optInt("rating", 0),
                                p.getString("href"),
                                p.optBoolean("favorite", false)
                            )
                        )
                    }
                    modules.add(HomeModule.Products(products, "Featured Products"))
                }
            }
        }

        val orderedModules = mutableListOf<HomeModule>()

        // Example: Banner first, then Category, then Featured
        modules.find { it is HomeModule.Banner }?.let { orderedModules.add(it) }
        modules.find { it is HomeModule.Category }?.let { orderedModules.add(it) }
        modules.find { it is HomeModule.Products }?.let { orderedModules.add(it) }

        return modules
    }
}
