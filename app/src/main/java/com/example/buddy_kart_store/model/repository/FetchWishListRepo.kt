package com.example.buddy_kart_store.model.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData

import com.example.buddy_kart_store.model.retrofit_setup.login.TrendingProduct
import com.example.buddy_kart_store.model.retrofit_setup.login.apiService
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class FetchWishListRepo(private val apiService: apiService) {

    // 🔹 LiveData for wishlist
    val wishlistLiveData = MutableLiveData<List<TrendingProduct>>()
    val wishedCountLiveData = MutableLiveData<Int>()


    fun fetchWishList(
        customerId: String,
        onResult: (Boolean, String, List<TrendingProduct>?, Int) -> Unit
    ) {
        apiService.fetchWishList(
            route = "wbapi/wbwishlist",
            customerId = customerId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    var keyCount = 0
                    val bodyString = response.body()?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val json = JSONObject(bodyString)

                        val success = json.optBoolean("success")
                        val message = json.optString("message")
                        val dataArray = json.optJSONArray("products")
                        Log.d("wishlist", "onResponse: $dataArray")

                        val productList = mutableListOf<TrendingProduct>()

                        if (success && dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val productObj = dataArray.getJSONObject(i)
                                val productId = productObj.optString("product_id")
                                val name = productObj.optString("name", "N/A")
                                    .replace(Regex("[^A-Za-z\\s]"), "")
                                    .trim()
                                val price = productObj.optString("price")
                                val image = productObj.optString("thumb")
                                val thumb = "https://hello.buddykartstore.com/image/$image"

                                productList.add(
                                    TrendingProduct(
                                        product_id = productId,
                                        name = name,
                                        price = price,
                                        imageUrl = thumb
                                    )
                                )
                                keyCount++
                            }

                            // 🔹 Update LiveData
                            wishlistLiveData.postValue(productList)
                            wishedCountLiveData.postValue(keyCount)

                            Log.d("wishlist", "Final List Size: ${productList.size}")
                            onResult(true, message, productList, keyCount)
                        } else {
                            onResult(false, message, null, 0)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult(false, "Parsing error", null, 0)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResult(false, t.localizedMessage ?: "API Error", null, 0)
            }
        })
    }


    fun addToWishlist(customerId: String, productId: String, onResult: (Boolean, String) -> Unit) {
        apiService.addToWishlist(route = "wbapi/wbwishlist.addProductToWishlist", customerId = customerId, productId = productId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    try {
                        val bodyString = response.body()?.string()
                        val json = JSONObject(bodyString ?: "")
                        val success = json.optString("success") == "true"
                        val message = json.optString("message")

                        if (success) {
                            // 🔹 Update LiveData: mark product as wished
                            wishlistLiveData.value = wishlistLiveData.value?.map {
                                if (it.product_id == productId) it.copy(isWished = true) else it
                            }
                        }

                        onResult(success, message)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onResult(false, e.message ?: "Error")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onResult(false, t.message ?: "Network Error")
                }
            })
    }

    fun removeFromWishlist(customerId: String, productId: String, onResult: (Boolean, String) -> Unit) {
        apiService.removeFromWishlist(route = "wbapi/wbwishlist.removeProductFromWishlist", customerId = customerId, productId = productId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    try {
                        val bodyString = response.body()?.string()
                        val json = JSONObject(bodyString ?: "")
                        val success = json.optString("success") == "true"
                        val message = json.optString("message")

                        if (success) {
                            // 🔹 Update LiveData: mark product as not wished
                            wishlistLiveData.value = wishlistLiveData.value?.map {
                                if (it.product_id == productId) it.copy(isWished = false) else it
                            }
                        }

                        onResult(success, message)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onResult(false, e.message ?: "Error")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onResult(false, t.message ?: "Network Error")
                }
            })
    }
}
