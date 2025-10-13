package com.example.buddy_kart_store.model.repository

import android.util.Log
import android.widget.Toast
import com.example.buddy_kart_store.model.retrofit_setup.login.Review
import com.example.buddy_kart_store.model.retrofit_setup.login.apiService
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ReviewRepo(private val apiService: apiService) {

//for post review

        fun addReview(
            customerId: String,
            productId: String,
            name: String,
            rating: String,
            review: String,
            callback: (success: Boolean, message: String) -> Unit
        ) {
            apiService.addReview(
                route = "wbapi/wbreviews.addreview",
                customerId = customerId,
                productId = productId,
                name = name,
                rating = rating,
                review = review
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val bodyString = response.body()?.string()
                        try {
                            val json = JSONObject(bodyString ?: "{}")
                            val success = json.optString("success") == "true"
                            val message = json.optString("message")
                            Log.d("postingreview", "onResponse: $json")
                            callback(success, message)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            callback(false, "Parsing error")
                        }
                    } else {
                        callback(false, "API Error: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    callback(false, "Network error: ${t.localizedMessage}")
                }
            })
        }





    fun fetchReview(
        customerId: String,
        productId: String,
        onResult: (List<Review>) -> Unit
    ) {
        apiService.fetchReview(
            route = "wbapi/wbreviews.getReviews",
            productId = productId,
            customerId = customerId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val bodyString = response.body()?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val json = JSONObject(bodyString)
                        Log.d("gettingjson", "onResponse: $json")

                        // Example: API response structure
                        val reviewArray = json.getJSONArray("reviews")
                        val list = mutableListOf<Review>()
                        Log.d("featvhrdreciew", "onResponse: $reviewArray")

                        for (i in 0 until reviewArray.length()) {
                            val obj = reviewArray.getJSONObject(i)
                            list.add(
                                Review(
                                    productId = obj.optString("product_id"),
                                    customerId = obj.optString("customer_id"),
                                    name = obj.optString("author"),
                                    rating = obj.optString("rating").toFloat(),
                                    review = obj.optString("text"),
                                    date = obj.optString("date_added")


                                )
                            )
                        }

                        onResult(list)
                    } else {
                        onResult(emptyList())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult(emptyList())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                onResult(emptyList())
            }
        })
    }


    fun getTotalReviews(
        customerId: String,
        productId: String,
    ) {
        apiService.GetTotalReviews(
            route = "wbapi/wbreviews.getTotalReviewCount",
            productId = productId,
            customerId = customerId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {

                try {
                    val bodyString = response.body()?.string()
                    if (!bodyString.isNullOrEmpty()){
                        val json = JSONObject(bodyString)
                        Log.d("addddddd", "onResponse: $json")

                    }

                }
                catch (e:Exception){
                    e.printStackTrace()
                }

            }

            override fun onFailure(
                p0: Call<ResponseBody?>,
                p1: Throwable
            ) {
                TODO("Not yet implemented")
            }


        })

    }
}