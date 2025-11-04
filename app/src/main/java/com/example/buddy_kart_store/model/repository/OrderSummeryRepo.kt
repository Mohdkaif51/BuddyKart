package com.example.buddy_kart_store.model.repository

import android.net.Uri
import android.util.Log
import com.example.buddy_kart_store.model.retrofit_setup.login.OrderDetail
import com.example.buddy_kart_store.model.retrofit_setup.login.OrderDetails
import com.example.buddy_kart_store.model.retrofit_setup.login.OrderItem
import com.example.buddy_kart_store.model.retrofit_setup.login.OrderSummary
import com.example.buddy_kart_store.model.retrofit_setup.login.apiService
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class OrderSummeryRepo(private val apiService: apiService) {

    fun fetchOrderDetails(orderId: String, customerId: String, onResult: (OrderDetail) -> Unit) {
        Log.d("gettingorderId", "fetchOrderDetails: $orderId")
        apiService.fetchOrderDetail(
            route = "wbapi/wborder.getorderinfo",
            orderId = orderId,
            customerId = customerId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val bodyString = response.body()?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val json = JSONObject(bodyString)
                        Log.d("summery", "onResponse: $json")

                        val orderInfo = json.getJSONObject("order_info")

                        // Parse products array
                        val productsArray = orderInfo.getJSONArray("products")
                        val itemsMap = mutableMapOf<String, OrderItem>() // key: product_id

                        for (i in 0 until productsArray.length()) {
                            val productJson = productsArray.getJSONObject(i)

                            // Determine product ID
                            val productId =
                                productJson.optString("product_id", productJson.optString("id", ""))
                            if (productId.isEmpty()) continue

                            // Check if this product is already added
                            if (itemsMap.containsKey(productId)) {
                                // Merge frontend info (image, formatted price)
                                val existingItem = itemsMap[productId]!!
                                val baseUrl = "https://hellobuddy.jkopticals.com/image/"
                                val rawImage = productJson.optString("image", existingItem.imageUrl)
                                Log.d("gettingimage", "onResponse: $rawImage")

                                val decodedImage = rawImage.replace("&amp;", "&")

                                val cleanImage = decodedImage.replace(" ", "%20")

                                val fullImageUrl = if (cleanImage.startsWith("http", ignoreCase = true)) {
                                    cleanImage
                                } else {
                                    baseUrl + cleanImage
                                }

//                                Log.d("imagetag", "Full image URL: $fullImageUrl")

                                val price = productJson.optString("total", existingItem.price)
                                itemsMap[productId] =
                                    existingItem.copy(price = price, imageUrl = fullImageUrl)
                            } else {
                                val baseUrl = "https://hellobuddy.jkopticals.com/image/"
                                val rawImage = productJson.optString("image")
                                Log.d("gettingimage", "onResponse: $rawImage")

                                val decodedImage = rawImage.replace("&amp;", "&")

                                val cleanImage = decodedImage.replace(" ", "%20")

                                val fullImageUrl = if (cleanImage.startsWith("http", ignoreCase = true)) {
                                    cleanImage
                                } else {
                                    baseUrl + cleanImage
                                }

                                val item = OrderItem(
                                    id = productId,
                                    name = productJson.optString("name", ""),
                                    quantity = productJson.optString("quantity", "0").toInt(),
                                    price = productJson.optString("total", ""),
                                    imageUrl = fullImageUrl
                                )
                                Log.d("imgaetagg", "onResponse: $item")
                                itemsMap[productId] = item
                            }
                        }

// Convert map to list
                        val items = itemsMap.values.toList()


                        // Parse totals
                        val totalsArray = orderInfo.getJSONArray("totals")
                        Log.d("gettingtax", "onResponse: $totalsArray")

                        var subTotal = ""
                        var deliveryCharge = ""
                        var taxAmount = ""
                        var grandTotal = ""

// Decimal formatter
                        val df = DecimalFormat("#.00")

                        for (i in 0 until totalsArray.length()) {
                            val totalJson = totalsArray.getJSONObject(i)
                            val title = totalJson.optString("title", "")
                            val text = totalJson.optString("text", "₹0.00")

                            // Extract numeric part safely and format
//                            val numericValue = text.replace(Regex("[^0-9.]"), "")
//                            val formattedValue = df.format(numericValue.toDoubleOrNull() ?: 0.0)

                            when {
                                title.contains("Sub-Total", ignoreCase = true) -> subTotal = text
                                title.contains("Shipping", ignoreCase = true) -> deliveryCharge = text
                                title.contains("Tax", ignoreCase = true) -> taxAmount = text
                                title.equals("Total", ignoreCase = true) -> grandTotal = text
                            }
                        }

// ✅ Log or display values
                        Log.d("TotalsParsed", "SubTotal=$subTotal, Shipping=$deliveryCharge, Tax=$taxAmount, Total=$grandTotal")

// Now all values are ready in 2-decimal format


                        val orderSummary = OrderSummary(
                            items = items,
                            subTotal = subTotal,
                            deliveryCharge = deliveryCharge,
                            flatshippingrate = deliveryCharge,
                            ecotax = taxAmount,
                            vat = deliveryCharge,
                            grandTotal = grandTotal
                        )
                        Log.d("summery", "onResponse: $orderSummary")

                        // Order attributes example (keywords)
                        val attributes = mutableListOf<String>()
                        // You can extract from JSON if available, for now dummy
                        attributes.add("Keyword 1")
                        attributes.add("Keyword 2")

                        // Order details
                        val orderDetails = OrderDetails(
                            orderId = orderInfo.optString("order_id", ""),
                            paymentMethod = orderInfo.getJSONObject("payment_method")
                                .optString("name", ""),
                            deliveryAddress = orderInfo.optString("shipping_address_1", "") + ", " +
                                    orderInfo.optString("shipping_city", "") + ", " +
                                    orderInfo.optString("shipping_zone", ""),
                            orderPlaced = orderInfo.optString("date_added", "")
                        )

                        val orderDetail = OrderDetail(
                            orderSummary = orderSummary,
                            orderAttributes = attributes,
                            orderDetails = orderDetails
                        )

                        onResult(orderDetail)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("summery", "API call failed: ${t.message}")
            }
        })
    }
}
