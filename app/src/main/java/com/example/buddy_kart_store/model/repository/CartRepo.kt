package com.example.buddy_kart_store.model.repository

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.buddy_kart_store.model.retrofit_setup.login.CartDetail
import com.example.buddy_kart_store.model.retrofit_setup.login.apiService
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback


class CartRepo(private val apiService: apiService) {


    //    for product fetch
    fun fetchCart(customerId: String, sessionId: String): MutableLiveData<List<CartDetail>> {
        val liveData = MutableLiveData<List<CartDetail>>()

        Log.d("fetchCart_debug", "customerId: $customerId")
        Log.d("fetchCart_debug", "sessionId: $sessionId")

        apiService.fetchCartItem(
            route = "wbapi/wbcart.cartProductListing",
            customerId = customerId,
            sessionId = sessionId
        ).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    Log.d("fetchCart_debug", "Request: ${call.request()}")
                    Log.d("fetchCart_debug", "Response: $response")

                    val responseBody = response.body()?.string() ?: "{}"
                    Log.d("fetchCart_debug", "ResponseBody: $responseBody")

                    val cartList = mutableListOf<CartDetail>()

                    val jsonObject = JSONObject(responseBody)

                    val productsObj = jsonObject.optJSONObject("products")
                    Log.d("fetchCart_debug", "Responseprohect: $productsObj")


                    if (productsObj != null) {
                        val keys = productsObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val item = productsObj.getJSONObject(key)

                            val cartItem = CartDetail(
                                cart_id = item.optString("cart_id", ""),
                                product_id = item.optString("product_id", ""),
                                name = item.optString("name", "").replace("&quot;", "\""),
                                image = item.optString("image", ""),
                                price = item.optDouble("price", 0.0),
                                quantity = item.optString("quantity", "0").toIntOrNull() ?: 0,
                                total = item.optDouble("total", 0.0)
                            )
                            cartList.add(cartItem)
                        }
                    } else {
                        Log.d("fetchCart_debug", "Products object is empty")
                    }

                    liveData.postValue(cartList)

                } catch (e: Exception) {
                    Log.e("fetchCart_debug", "Parsing error", e)
                    liveData.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("fetchCart_debug", "API call failed: ${t.message}")
                liveData.postValue(emptyList())
            }
        })

        return liveData
    }


//    for add product

    fun addToCart(
        customerId: String,
        sessionId: String,
        productId: String
    ) {
        apiService.addToCart(
            route = "wbapi/wbcart.addtocart",
            customerId = customerId,
            sessionId = sessionId,
            productId = productId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("addToCart", "customerId: $customerId")
                Log.d("addToCart", "sessionId: $sessionId")
                Log.d("addToCart", "productId: $productId")

                if (response.isSuccessful) {
                    val rawResponse = response.body()?.string()
                    Log.d("addToCart", "Response: $rawResponse")

                    rawResponse?.let {
                        try {
                            val json = JSONObject(it)
                            val success = json.optBoolean("success", false)
                            val message = json.optString("message", "")
                            Log.d("addToCart", "Success: $success, Message: $message")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    Log.e("addToCart", "HTTP error code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("addToCart", "API call failed: ${t.message}", t)
            }
        })
    }

}
