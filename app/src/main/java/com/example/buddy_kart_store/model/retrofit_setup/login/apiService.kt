package com.example.buddy_kart_store.model.retrofit_setup.login

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface apiService {


    @FormUrlEncoded
    @POST("index.php")
    @Headers(
        "Accept: application/json", "Content-Type: application/x-www-form-urlencoded"
    )
    fun loginUser(
        @Query("route") route: String = "wbapi/wblogin.getlogin",
        @Field("customer_id") customerId: String = "",
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ResponseBody>


//    https://staging.buddykartstore.com/index.php?route=wbapi/wblogin.getlogin


    // Register
    @FormUrlEncoded
    @POST("index.php")
    @Headers(
        "Accept: application/json", "Content-Type: application/x-www-form-urlencoded"
    )
    fun registerUser(
        @Query("route") route: String = "wbapi/wbregister.register",
        @Field("customer_group_id") customerId: String = "",
        @Field("firstname") firstname: String,
        @Field("lastname") lastname: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("telephone") telephone: String
    ): Call<ResponseBody>


    @GET("index.php")
    fun getCategory(
        @Query("route") route: String,
        @Query("category_id") category_id: Int,
        @Query("page") page: Int = 1 // default page 1

    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun getCategoryProduct(
        @Query("route") route: String = "wbapi/productapi.getproduct",
        @Field("category_id") category_id: String,
        @Field("page") page: Int = 1 // default page 1
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php?route=wbapi/searchquery.searchproduct")
    suspend fun searchProducts(
        @Field("route") route: String,
        @Field("search") search: String
    ): Response<ResponseBody>


    @FormUrlEncoded
    @POST("index.php?route=wbapi/wbproductapi.getproduct")
    fun getProduct(
        @Field("route") route: String,

        ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun getProductsDetail(
        @Query("route") route: String = "wbapi/wbproductapi.getproduct",
        @Field("product_id") productId: String
    ): Call<ResponseBody>


    //    profile
    @FormUrlEncoded
    @Headers("Accept: application/json")

    @POST("index.php")
    fun getAccountDetails(
        @Query("route") route: String = "wbapi/wbaccount.getaccountupdate",
        @Field("customer_id") customerId: String,
        @Field("firstname") firstname: String,
        @Field("lastname") lastname: String,
        @Field("email") email: String,
        @Field("telephone") telephone: String,
        @Field("custom_field") custom_field: () -> Unit
    ): Call<ResponseBody>

    //    resetpassword

    @FormUrlEncoded
    @POST("index.php")
    @Headers("Accept: application/json")

    fun requestForgetPassword(
        @Query("route") route: String = "wbapi/wbforgetpass.getforgetpass",
        @Field("customer_id") customerId: String,
        @Field("email") email: String,
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun changePassword(
        @Query("route") route: String = "wbapi/passwordchange.change",
        @Field("customer_id") customerId: String,
        @Field("password") newPassword: String,
        @Field("confirm") confirmPassword: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun addAddress(
        @Query("route") route: String = "wbapi/addaddressapi.adaddress",
        @Field("customer_id") customerId: String,
        @Field("firstname") firstname: String,
        @Field("lastname") lastname: String,
        @Field("company") company: String,
        @Field("address_1") address_1: String,
        @Field("address_2") address_2: String,
        @Field("city") city: String,
        @Field("postcode") postcode: String,
        @Field("country_id") country_id: String,
        @Field("zone_id") zone_id: String
    ): Call<ResponseBody>

    //    @FormUrlEncoded
    @POST("index.php")
    fun fetchCountries(
        @Query("route") route: String = "wbapi/countryapi.getcountry",

        ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun getstate(
        @Query("route") route: String = "wbapi/countryapi.getcountry",
        @Field("country_id") countryId: Int


    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("index.php")
    fun getAllAddress(
        @Query("route") route: String = "wbapi/addressapi.getaddress",
        @Field("customer_id") customerId: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun deleteAddress(
        @Query("route") route: String = "wbapi/wbaddressdel.addressDelete",
        @Field("address_id") addressId: String,
        @Field("customer_id") customerId: String

    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("index.php")
    fun updateAddress(
        @Query("route") route: String = "wbapi/updateaddressapi.updateaddress",
        @Field("address_id") addressId: String,
        @Field("customer_id") customerId: String,
        @Field("firstname") firstname: String,
        @Field("lastname") lastname: String,
        @Field("company") company: String,
        @Field("address_1") address_1: String,
        @Field("address_2") address_2: String,
        @Field("city") city: String,
        @Field("postcode") postcode: String,
        @Field("country_id") country_id: String,
        @Field("zone_id") zone_id: String
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("index.php")
    fun fetchWishList(
        @Query("route") route: String = "wbapi/wbwishlist",
        @Field("customer_id") customerId: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun addToWishlist(
        @Query("route") route: String = "wbapi/wbwishlist.addProductToWishlist",
        @Field("customer_id") customerId: String,
        @Field("product_id") productId: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun removeFromWishlist(
        @Query("route") route: String = "wbapi/wbwishlist.removeProductFromWishlist",
        @Field("customer_id") customerId: String,
        @Field("product_id") productId: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun fetchOrder(
        @Query("route") route: String = "wbapi/wborder.getorder",
        @Field("customer_id") customerId: String
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("index.php")
    fun fetchOrderDetail(
        @Query("route") route: String = "wbapi/wborder.getorderinfo",
        @Field("order_id") orderId: String,
        @Field("customer_id") customerId: String
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("index.php")
    fun addReview(
        @Query("route") route: String = "wbapi/wbreviews.addreview",
        @Field("customer_id") customerId: String,
        @Field("product_id") productId: String,
        @Field("name") name: String,
        @Field("rating") rating: String,
        @Field("text") review: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun fetchReview(
        @Query("route") route: String = "wbapi/wbreviews.getReviews",
        @Field("product_id") productId: String,
        @Field("customer_id") customerId: String
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("index.php")
    fun GetTotalReviews(
        @Query("route") route: String = "wbapi/wbreviews.getTotalReviewCount",
        @Field("product_id") productId: String,
        @Field("customer_id") customerId: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun relatedProduct(
        @Query("route") route: String = "wbapi/wbproductapi.getproduct",
        @Field("product_id") productId: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun fetchCartItem(
        @Query("route") route: String = "wbapi/wbcart.cartProductListing",          // route stays in URL
        @Field("customerId") customerId: String,
        @Field("session_id") sessionId: String
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("index.php")
    fun addToCart(
        @Query("route") route: String = "wbapi/wbcart.addtocart",
        @Field("customer_id") customerId: String,
        @Field("session_id") sessionId: String,
        @Field("product_id") productId: String

    ): Call<ResponseBody>

    @GET("https://hellobuddy.jkopticals.com/index.php?route=wbapi/homepageapi.gethomepage")
    suspend fun getHome(): Response<ResponseBody>


    @FormUrlEncoded
    @POST("index.php")
    fun deleteProduct(
        @Query("route") route: String = "wbapi/wbcart.removecartproducts",
        @Field("customer_id") customerId: String,
        @Field("session_id") sessionId: String,
        @Field("cart_id") cartid: String,
        @Field("product_id") productId: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("index.php")
    fun deleteCart(
        @Query("route") route: String = "wbapi/wbcart.clearcart",
        @Field("customer_id") customerId: String,
        @Field("session_id") sessionId: String
//        @Field("cart_id") cartid: String
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("index.php")
    fun updateQuantity(
        @Query("route") route: String = "wbapi/wbcart.updateCartQuantity",
        @Field("customer_id") customerId: String,
        @Field("session_id") sessionId: String,
        @Field("cart_id") cartId: String,
        @Field("quantity") quantity: String

    ): Call<ResponseBody>

}




