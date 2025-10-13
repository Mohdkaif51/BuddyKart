package com.example.buddy_kart_store.model.repository

import android.annotation.SuppressLint
import android.text.Html
import android.util.Log
import com.example.buddy_kart_store.model.retrofit_setup.login.RelatedImage
import com.example.buddy_kart_store.model.retrofit_setup.login.RelatedProduct
import com.example.buddy_kart_store.model.retrofit_setup.login.apiService
import okhttp3.ResponseBody
import org.json.JSONObject
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RelatedProductRepo(private val apiService: apiService) {

    fun fetchRelatedProducts(productId: String, onResult: (List<RelatedProduct>) -> Unit) {
        apiService.relatedProduct(
            route = "wbapi/wbproductapi.getproduct",
            productId = productId
        ).enqueue(object : Callback<ResponseBody> {
            @SuppressLint("DefaultLocale")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val bodyString = response.body()?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val json = JSONObject(bodyString)
                        val dataArray = json.optJSONArray("related_products")
                        val products = mutableListOf<RelatedProduct>()

                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val obj = dataArray.getJSONObject(i)
                                val rawimg = obj.optString("image")
                                val baseUrl = "https://hello.buddykartstore.com/"
                                val image =
                                    if (rawimg.startsWith("http")) rawimg else baseUrl + "image/" + rawimg

                                val raw = obj.getString("name")
                                val name = raw.replace(Regex("[^A-Za-z\\s]"), "").trim()
                                val descriptionHtml = obj.optString("description", "")
                                val doc = Jsoup.parse(descriptionHtml)
                                doc.select("script, style").remove()
                                val formattedText = Html.fromHtml(doc.text(), Html.FROM_HTML_MODE_LEGACY).toString().trim()


                                products.add(
                                    RelatedProduct(
                                        productId = obj.optString("product_id"),
                                        name ,
                                        price = String.format("%.2f", obj.optDouble("price")),
                                        image = image,
                                        Wished = false,
                                        description = formattedText

                                    )
                                )
                            }
                        }

                        val imgArray = json.optJSONArray("image")


                        onResult(products)
                    } else {
                        onResult(emptyList())
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult(emptyList())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("related", "API call failed: ${t.message}")
                onResult(emptyList())
            }
        })
    }


    fun fetchRelatedImages(productId: String, onResult: (List<RelatedImage>) -> Unit) {
        apiService.relatedProduct(
            route = "wbapi/wbproductapi.getproduct",
            productId = productId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val bodyString = response.body()?.string()
                    val imagesList = mutableListOf<RelatedImage>()

                    if (!bodyString.isNullOrEmpty()) {
                        val json = JSONObject(bodyString)
                        val dataArray = json.optJSONArray("image")
                        val baseUrl = "https://hello.buddykartstore.com/"
                        Log.d("gettingimages", "onResponse: $dataArray")

                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val obj = dataArray.getJSONObject(i)
                                val rawImg = obj.optString("image")
                                val image = if (rawImg.startsWith("http")) rawImg else "$baseUrl/image/$rawImg"

                                imagesList.add(
                                    RelatedImage(
                                        productId = obj.optString("product_id"),
                                        image = image
                                    )
                                )
                            }
                        }
                    }

                    onResult(imagesList)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult(emptyList())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("RelatedImages", "API call failed: ${t.message}")
                onResult(emptyList())
            }
        })
    }

}

