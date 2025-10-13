package com.example.buddy_kart_store.model.repository

import android.util.Log
import com.example.buddy_kart_store.model.retrofit_setup.login.apiService
import com.example.buddy_kart_store.model.retrofit_setup.login.order
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FetchOrderRepo(private val apiService: apiService) {

    fun fetchOrder(customerId: String, onResult: (List<order>) -> Unit) {
        Log.d("gettingcustomerid", "fetchOrder: $customerId")
        apiService.fetchOrder(
            route = "wbapi/wborder.getorder",
            customerId = customerId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val orders = mutableListOf<order>()
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val raw = response.body()!!.string()
                        val startIndex = raw.indexOf("{")
                        val endIndex = raw.lastIndexOf("}")
                        if (startIndex != -1 && endIndex != -1) {
                            val cleanJson = raw.substring(startIndex, endIndex + 1)
                            val jsonObject = JSONObject(cleanJson)
                            Log.d("orderresponse", "onResponse: $jsonObject")
                            val jsonArray = jsonObject.getJSONArray("orders")
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                orders.add(
                                    order(
                                        orderId = obj.getString("order_id"),
                                        customerName = obj.getString("firstname") + " " + obj.getString("lastname"),
                                        total = obj.getString("total"),
                                        dateAdded = obj.getString("date_added"),
                                        orderStatus = obj.getString("order_status_id"),
                                        shippingAddress = obj.getString("shipping_address_1") + ", " + obj.getString("shipping_city"),
                                        paymentMethod = JSONObject(obj.getString("payment_method")).getString("name")
                                    )
                                )

                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FetchOrderRepo", "JSON parse error: ${e.message}")
                    }
                }
                onResult(orders)
                Log.d("FetchOrderRepo", "Orders: $orders")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("FetchOrderRepo", "Network error: ${t.message}")
                onResult(emptyList())
            }
        })
    }
}
