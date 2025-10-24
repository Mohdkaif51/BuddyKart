package com.example.buddy_kart_store.model.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.buddy_kart_store.model.retrofit_setup.login.BillingAmt
import com.example.buddy_kart_store.model.retrofit_setup.login.CartDetail
import com.example.buddy_kart_store.model.retrofit_setup.login.apiService
import com.example.buddy_kart_store.utils.Sharedpref
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CartRepo(private val apiService: apiService, private val context: Context) {


    //    for product fetch
    fun fetchCart(
        customerId: String, sessionId: String
    ): Pair<MutableLiveData<List<CartDetail>>, MutableLiveData<BillingAmt>> {

        Log.d("gettingcustomerId", "fetchCart: $customerId")

        val cartLiveData = MutableLiveData<List<CartDetail>>()
        val billingLiveData = MutableLiveData<BillingAmt>()
        val cartCountLiveData = MutableLiveData<Int>()
        val quantitiesMap = mutableMapOf<String, Int>()


        apiService.fetchCartItem(
            route = "wbapi/wbcart.cartProductListing",
            customerId = customerId,
            sessionId = sessionId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val responseBody = response.body()?.string() ?: "{}"
                    val jsonObject = JSONObject(responseBody)
                    val cartList = mutableListOf<CartDetail>()

                    val productsObj = jsonObject.optJSONObject("getProducts")
                    val productIdsSet = mutableSetOf<String>() // To store all product IDs

                    val cartIdSet = mutableSetOf<String>()
                    val quantityMap = mutableMapOf<String, Int>()

                    var keyCount = 0
                    if (productsObj != null) {
                        val keys = productsObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val item = productsObj.getJSONObject(key)
                            val thumb =
                                "https://hello.buddykartstore.com/image/${item.optString("image")}"

                            val totalSub =
                                item.optDouble("total", 0.0).toInt().toString() // remove decimals

                            val cartItem = CartDetail(
                                cart_id = item.optString("cart_id", ""),
                                product_id = item.optString("product_id", ""),
                                name = item.optString("name", "").replace("&quot;", "\""),
                                image = thumb,
                                price = item.optDouble("price", 0.0).toInt()
                                    .toDouble(), // remove decimals
                                quantity = item.optString("quantity", "0").toIntOrNull() ?: 0,
                                subtotal = totalSub,
                                total = totalSub

                            )
                            cartList.add(cartItem)
                            productIdsSet.add(cartItem.product_id)
                            cartIdSet.add(cartItem.cart_id)
                            quantityMap[cartItem.product_id] = cartItem.quantity

//                           for cartId
                            Sharedpref.CartPref.saveCartMapping(
                                context,
                                cartItem.product_id,
                                cartItem.cart_id
                            )



                            keyCount++
                        }
                        Sharedpref.CartPref.saveCartQuantities(context, quantityMap)

//                        for
                        Log.d("DEBUG_CART_IDS", "Saved cart IDs: $cartIdSet")
//                        to save productid
                        Sharedpref.CartPrefs.saveCartIds(context, productIdsSet)
                        Log.d("DEBUG_CART_IDS", "Saved cart IDs: $productIdsSet")
//                        Sharedpref.CartPref.saveCartQuantities(
//                            context,
//                            quantity as Map<String, Int>
//                        )

                    }

                    cartLiveData.postValue(cartList)
                    cartCountLiveData.postValue(keyCount)


                    // Parse billing info
                    val subTotal = jsonObject.optDouble("subTotal", 0.0).toInt().toDouble()
                    val total = jsonObject.optDouble("total", 0.0).toInt().toDouble()
                    val taxObject = jsonObject.optJSONObject("tax")
                    Log.d("gettingtax", "onResponse: $taxObject")

                    val tax = taxObject?.let { obj ->
                        var sum = 0.0
                        val keys = obj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            sum += obj.optDouble(key, 0.0)
                        }
                        sum.toInt().toDouble() // remove decimals
                    } ?: 0.0

                    billingLiveData.postValue(BillingAmt(subTotal, tax, total))

                } catch (e: Exception) {
                    cartLiveData.postValue(emptyList())
//                billingLiveData.postValue(BillingAmt())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                cartLiveData.postValue(emptyList())
//            billingLiveData.postValue(BillingAmt())
            }
        })

        return Pair(cartLiveData, billingLiveData)
    }

//    for add product


    fun addToCart(
        customerId: String,
        sessionId: String,
        productId: String,
        onResult: (Boolean, String) -> Unit
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
                    try {
                        val bodyString = response.body()?.string() ?: "{}"
                        Log.d("deleteProduct_debug", "Response: $bodyString")

                        val jsonObject = JSONObject(bodyString)
                        val status = jsonObject.optBoolean("success", false)
                        val message = jsonObject.optString("message", "Product removed")

                        if (status) {
                            onResult(true, message)
                        } else {
                            onResult(false, message)
                        }
                    } catch (e: Exception) {
                        Log.e("deleteProduct_debug", "Parsing error: ${e.message}")
                        onResult(false, "Parsing error: ${e.message}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("deleteProduct_debug", "Error Response: $errorBody")
                    onResult(false, "Failed to remove product: $errorBody")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("addToCart", "API call failed: ${t.message}", t)
            }
        })
    }


//delete product

    fun deleteProduct(
        customerId: String,
        sessionId: String,
        productId: String,
        cart_id: String,
        onResult: (Boolean, String) -> Unit // ✅ callback for success or error message
    ) {
        apiService.deleteProduct(
            route = "wbapi/wbcart.removecartproducts",
            customerId = customerId,
            sessionId = sessionId,
            cartid = cart_id,
            productId = productId
        ).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(
                call: Call<ResponseBody>, response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    try {
                        val bodyString = response.body()?.string() ?: "{}"
                        Log.d("deleteProduct_debug", "Response: $bodyString")


                        val jsonObject = JSONObject(bodyString)
                        val productId = jsonObject.optString("productId", "")
                        val status = jsonObject.optBoolean("success", false)
                        val message = jsonObject.optString("message", "Product removed")


                        if (status) {
                            onResult(true, message)
                        } else {
                            onResult(false, message)
                        }
                    } catch (e: Exception) {
                        Log.e("deleteProduct_debug", "Parsing error: ${e.message}")
                        onResult(false, "Parsing error: ${e.message}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("deleteProduct_debug", "Error Response: $errorBody")
                    onResult(false, "Failed to remove product: $errorBody")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("deleteProduct_debug", "API call failed: ${t.message}")
                onResult(false, "Network error: ${t.message}")
            }
        })
    }

//delete cart

    fun deleteCart(
        customerId: String, sessionId: String, onResult: (Boolean, String) -> Unit
    ) {
        apiService.deleteCart(
            route = "wbapi/wbcart.clearcart", customerId = customerId, sessionId = sessionId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {

                if (response.isSuccessful) {
                    val bodyString = response.body()?.string() ?: "{}"

                    try {
                        val jsonObject = JSONObject(bodyString)
                        val success = jsonObject.optBoolean("success", false)
                        val message = jsonObject.optString("message", "")

                        if (success) {
                            Log.d("deleteCart", "Success: $message")


                        } else {
                            Log.e("deleteCart", "Error: $message")
                        }
                    } catch (e: Exception) {
                        Log.e("deleteCart", "Parsing error: ${e.message}")
                    }
                }
            }

            override fun onFailure(
                p0: Call<ResponseBody?>, p1: Throwable
            ) {
                TODO("Not yet implemented")
            }

        })
    }


//    update quantity


    fun updateQuantity(
        customerId: String,
        sessionId: String,
        cartId: String,
        quantity: String,
        onResult: (Boolean, String) -> Unit
    ) {
        Log.d("updateQuantity", "customerId: $customerId")
        Log.d("updateQuantity", "sessionId: $sessionId")
        Log.d("updateQuantity", "cartId: $cartId")
        Log.d("updateQuantity", "quantity: $quantity")

        apiService.updateQuantity(
            route = "wbapi/wbcart.updateCartQuantity",
            customerId = customerId,
            sessionId = sessionId,
            cartId = cartId,
            quantity = quantity
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    Log.d("updateQuantity", "Response: $response")

                    val bodyString = response.body()?.string() ?: "{}"
                    Log.d("updateQuantity", "Response: $bodyString")

                    try {

                        val jsonObject = JSONObject(bodyString)

                        val success = jsonObject.optBoolean("success", false)
                        val message = jsonObject.optString("message", "")

                        if (success) {
                            onResult(true, message)
                        } else {
                            onResult(false, message)
                        }
                    } catch (e: Exception) {
                        Log.e("updateQuantity", "Parsing error: ${e.message}")
                        onResult(false, "Parsing error: ${e.message}")
                    }


                }
            }

            override fun onFailure(
                p0: Call<ResponseBody?>, p1: Throwable
            ) {
                TODO("Not yet implemented")
            }

        })
    }




}
