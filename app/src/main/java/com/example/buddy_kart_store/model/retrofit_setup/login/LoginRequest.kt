package com.example.buddy_kart_store.model.retrofit_setup.login

import android.annotation.SuppressLint
import android.app.appsearch.StorageInfo
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


//login/signup

data class LoginRequest(
    @SerializedName("user_email")
    val email: String,

    @SerializedName("user_pass")
    val password: String
)

data class RegisterRequest(
    val route: String = "wbapi/wbregister.register",
    val customer_group_id: String = "",
    val firstname: String,
    val lastname: String,
    val email: String,
    val password: String,
    val telephone: String
)

//trendingProduct


data class TrendingProduct(
    val product_id: String,
    val name: String,
    val price: String,
    val imageUrl: String,
    var isWished: Boolean = false
)

data class HomeProduct(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: String,
    @SerializedName("discount") val discount: String,
    @SerializedName("image") val image: String?
)


//productdetail

data class ProductDetailResponse(
    val success: Boolean,
    val data: ProductDetail
)

data class ProductDetail(
    val id: String,
    val name: String,
    val price: String,
    val discountPrice: String,
    val rating: String,
    val description: String,
    val imageUrl: String
)
//-----------------------------------


//category
data class Categorys(

    val image: Int

)


data class categories(
    val name: String,
    val id: String,
    val image: String,
    val children: List<categories> = emptyList()

)


data class SearchProduct(
    var productId: String,
    var name: String,
    var imageUrl: String,
    var price: String,
    var rating: String,
    var favorite: Boolean


)

//cartdetail


data class CartDetail(

    val cart_id: String,
    val product_id: String,
    val name: String,
    val image: String,
    val price: Double,
    var quantity: Int,
    val total: Double
)


//add to cart
data class CartItem(
    val productId: String,
    val name: String,
    val price: String,
    val imageUrl: String,
    val quantity: Int = 1,
    var favorite: Boolean = false
)

data class CategoryProduct(
    var productId: Int,
    var name: String,
    var imageUrl: String,
    var price: String,
    var discount: String
)

@SuppressLint("ParcelCreator")
@Parcelize
data class Address(
    val addressId: String,
    val customerId: String,
    val firstname: String,
    val lastname: String,
    val company: String,
    val address1: String,
    val address2: String,
    val city: String,
    val postcode: String,
    val country: String,
    val zone: String,
    val countryId: String,   // ✅ ID for API
    val zoneId: String
) : Parcelable


data class OrderList(
    var productId: String,
    var name: String,
    var image: String,
    var price: String,
    var quantity: String,
    var status: String,
)

data class Country(
    val id: String,
    val name: String
) {
    override fun toString(): String = name
}

data class state(
    val id: String,
    val name: String
) {
    override fun toString(): String = name
}

data class order(
    val orderId: String,
    val dateAdded: String,
    val total: String,
    val customerName: String,
    val paymentMethod: String,
    val shippingAddress: String,
    val orderStatus: String
)


//-------------orderdetail--------------------

data class OrderDetail(
    val orderSummary: OrderSummary,
    val orderAttributes: List<String>,
    val orderDetails: OrderDetails
)

data class OrderSummary(
    val items: List<OrderItem>,
    val subTotal: String,
    val deliveryCharge: String,
    val flatshippingrate: String,
    val ecotax: String,
    val vat: String,
    val grandTotal: String
)

data class OrderItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: String,
    val imageUrl: String
)

data class OrderDetails(
    val orderId: String,
    val paymentMethod: String,
    val deliveryAddress: String,
    val orderPlaced: String
)

//-------------------------------------------------------------
data class Review(
    var productId: String,
    var customerId: String,
    var name: String,
    var rating: Float,
    var review: String,
    var date: String
)

data class RelatedProduct(
    val productId: String,
    val name: String,
    val price: String,
    val image: String,
    var Wished: Boolean,
    val description: String
)
data class RelatedImage(
    val productId: String,
    val image: String,
)
// Banner.kt
data class Banner(
    val title: String,
    val link: String,
    val image: String
)

// Category.kt
data class TopCategory(
    val id: String,
    val name: String,
    val image: String,
    val href: String
)

// FeaturedProduct.kt (optional)
data class FeaturedProduct(
    val productId: String,
    val name: String,
    val image: String,
    val description: String,
    val price: String,
    val rating: Int,
    var favorite: String,

    var Wished: Boolean
)

