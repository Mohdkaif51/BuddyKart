package com.example.buddy_kart_store.ui.Home

import android.R.attr.maxLines
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.util.Log.e
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.databinding.ActivityProductDetailPageBinding
import com.example.buddy_kart_store.model.repository.FetchWishListRepo
import com.example.buddy_kart_store.model.repository.RelatedProductRepo
import com.example.buddy_kart_store.model.repository.ReviewRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.CartItem
import com.example.buddy_kart_store.model.retrofit_setup.login.HomeProduct
import com.example.buddy_kart_store.model.retrofit_setup.login.RelatedImage
import com.example.buddy_kart_store.model.retrofit_setup.login.RelatedProduct
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.ui.drawer_section.MyWishlist
import com.example.buddy_kart_store.ui.recyclerviews.ProductReviewRecycler
import com.example.buddy_kart_store.ui.recyclerviews.RelatedImagesPagerAdapter
import com.example.buddy_kart_store.ui.recyclerviews.RelatedProductAdapter
import com.example.buddy_kart_store.ui.viewmodel.ReviewVM
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.relatedProoductVM
import com.example.buddy_kart_store.utils.CartManager
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.GenericViewModelFactory
import com.example.buddy_kart_store.utlis.SessionManager
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailPage : AppCompatActivity() {


    companion object {
        private const val EXTRA_PRODUCT_ID = "productId"

        fun launch(context: Context, productId: String) {
            val intent = Intent(context, ProductDetailPage::class.java)
            intent.putExtra(EXTRA_PRODUCT_ID, productId)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityProductDetailPageBinding
    private var currentProduct: CartItem? = null
    var isExpanded = false

    private val productList = mutableListOf<HomeProduct>()
    private lateinit var adapterpdp: RelatedImagesPagerAdapter

    private lateinit var relatedAdapter: RelatedProductAdapter

    private var isFavorite: Boolean = false

    private lateinit var viewModel: ReviewVM
    private lateinit var addViewModel: WishListVM
    private lateinit var fetchRelatedProducts: relatedProoductVM
    private lateinit var adapter: ProductReviewRecycler
    private var productId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // -------------------- Back Button --------------------
        binding.back.setOnClickListener { onBackPressed() }

        // -------------------- Description Expand/Collapse --------------------
        binding.descText.post {
            binding.seeMoreBtn.visibility =
                if (binding.descText.lineCount < 3) View.VISIBLE else View.GONE
        }
        var isExpanded = false
        binding.seeMoreBtn.setOnClickListener {
            if (isExpanded) {
                binding.descText.maxLines = 3
                binding.descText.ellipsize = TextUtils.TruncateAt.END
                binding.seeMoreBtn.text = "See More"
            } else {
                binding.descText.maxLines = Int.MAX_VALUE
                binding.descText.ellipsize = null
                binding.seeMoreBtn.text = "See Less"
            }
            isExpanded = !isExpanded
        }

        // -------------------- Wishlist Button --------------------
        val repository = FetchWishListRepo(RetrofitClient.iInstance)
        val addFactory = GenericViewModelFactory { WishListVM(repository) }
        addViewModel = ViewModelProvider(this, addFactory)[WishListVM::class.java]

        binding.favbtn.setOnClickListener {
            val product = currentProduct ?: return@setOnClickListener
            val customerId = SessionManager.getCustomerId(this) ?: return@setOnClickListener

            if (!product.favorite) {
                addViewModel.addToWishlist(productId.toString(), customerId) { success, message ->
                    if (success) {
                        product.favorite = true
                        updateFavIcon(true) // show "in wishlist" icon
                    } else {
                        updateFavIcon(false)
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                addViewModel.removeFromWishlist(productId.toString(), customerId) { success, message ->
                    if (success) {
                        product.favorite = false
                        updateFavIcon(false) // show "not in wishlist" icon
                    } else {
                        updateFavIcon(true)
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.wishlistbtn.setOnClickListener {
            val intent = Intent(this, MyWishlist::class.java)
            intent.putExtra("fromActivity", "PDP")
            startActivity(intent)
        }

        currentProduct?.let { product ->
            updateFavIcon(product.favorite)
        }
        // -------------------- Cart Button --------------------
        binding.cartbtn.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        // -------------------- Price Strike-through --------------------
        binding.actualprice.paintFlags =
            binding.actualprice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        // -------------------- Get Product ID --------------------
        productId = intent.getStringExtra(EXTRA_PRODUCT_ID) ?: ""
        if (productId.isNullOrEmpty()) {
            Toast.makeText(this, "No Product ID received", Toast.LENGTH_SHORT).show()
            return
        }
        fetchProductDetails(productId!!.toInt())

        // -------------------- Add to Cart --------------------
        binding.addtocartbtn.setOnClickListener {
            currentProduct?.let {
                CartManager.addToCart(it)
                Toast.makeText(this, "${it.name} added to cart", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "Product not loaded yet", Toast.LENGTH_SHORT).show()
        }

        // -------------------- Review Section --------------------
        val sharedPref = Sharedpref(this)
        val name = sharedPref.getName() ?: "Guest"
        val customerId = SessionManager.getCustomerId(this)

        // Initialize adapter & RecyclerView
        adapter = ProductReviewRecycler(
            mutableListOf()        )
        binding.reviewList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.reviewList.adapter = adapter

        // Init ViewModel
        val factory = GenericViewModelFactory { ReviewVM(ReviewRepo(RetrofitClient.iInstance)) }
        viewModel = ViewModelProvider(this, factory)[ReviewVM::class.java]

        // Observe reviews
        viewModel.reviews.observe(this) { reviews ->
            if (reviews.isNullOrEmpty()) {
                binding.noProductText.visibility = View.VISIBLE
                binding.reviewList.visibility = View.GONE
            } else {
                binding.noProductText.visibility = View.GONE
                binding.reviewList.visibility = View.VISIBLE
                adapter.updateList(reviews)
            }
        }

        // Submit review
        binding.submitReviewBtn.setOnClickListener {
            val reviewText = binding.userCommentInput.text.toString().trim()
            val ratingValue = binding.userRatingBar.rating

            if (customerId.isNullOrEmpty()) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (ratingValue == 0f) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (reviewText.isEmpty()) {
                Toast.makeText(this, "Please enter a review", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loader
            binding.progressbar.visibility = View.VISIBLE
            binding.submitReviewBtn.isEnabled = false

            viewModel.addReview(
                customerId = customerId,
                productId = productId.toString(),
                name = name,
                rating = ratingValue.toString(),
                review = reviewText
            )
        }

        // Observe submission result
        viewModel.reviewResult.observe(this) { (success, message) ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            binding.progressbar.visibility = View.GONE
            binding.submitReviewBtn.isEnabled = true

            if (success) {
                binding.userCommentInput.text?.clear()
                binding.userRatingBar.rating = 0f
                viewModel.fetchReview(customerId.toString(), productId.toString())
            }
        }

        // Fetch reviews initially
        viewModel.fetchReview(customerId.toString(), productId.toString())

        // -------------------- Related Products --------------------
        val repoRelated = RelatedProductRepo(RetrofitClient.iInstance)
        val relatedFactory = GenericViewModelFactory { relatedProoductVM(repoRelated) }
        fetchRelatedProducts =
            ViewModelProvider(this, relatedFactory)[relatedProoductVM::class.java]

        relatedAdapter = RelatedProductAdapter(
            emptyList(),
            onItemClick = { product ->
                launch(this, product.productId)
            },
            wishViewModel = addViewModel,
        )

        binding.relatedproduct.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.relatedproduct.adapter = relatedAdapter

        fetchRelatedProducts.relatedProducts.observe(this) { products ->
            relatedAdapter.updateList(products)
        }

        productId?.let { fetchRelatedProducts.fetchRelatedProducts(it) }



        adapterpdp = RelatedImagesPagerAdapter(emptyList())
        binding.relatedImagesViewPager.adapter = adapterpdp

        // Observe LiveData
        fetchRelatedProducts.relatedImages.observe(this) { images ->
            adapterpdp.updateList(images)
        }

        // Fetch related images using productId
        val productId = intent.getStringExtra("PRODUCT_ID") ?: ""
        Log.d("gettingcode", "onCreate: $productId")
        if (productId.isNotEmpty()) {
            fetchRelatedProducts.fetchRelatedImages(productId)
        }


    }

    @SuppressLint("SetTextI18n")
    private fun fetchProductDetails(productId: Int) {
        Log.d("gettingproduct", "fetchProductDetails: $productId")

        RetrofitClient.iInstance.getProductsDetail(
            route = "wbapi/wbproductapi.getproduct",
            productId = productId
        ).enqueue(object : Callback<ResponseBody> {

            @SuppressLint("DefaultLocale")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful || response.body() == null) {
                    Toast.makeText(this@ProductDetailPage, "Failed to fetch product", Toast.LENGTH_SHORT).show()
                    return
                }

                try {
                    val jsonString = response.body()!!.string()
                    val json = JSONObject(jsonString)
                    val data = json.opt("data")

                    // --- Safely extract product object ---
                    val productObj: JSONObject? = when (data) {
                        is JSONObject -> data
                        is JSONArray -> (0 until data.length())
                            .map { data.getJSONObject(it) }
                            .firstOrNull { it.optString("product_id") == productId.toString() }
                        else -> null
                    }

                    if (productObj == null) {
                        Toast.makeText(this@ProductDetailPage, "Product not found", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // --- Parse main fields ---
                    val baseUrl = "https://staging.buddykartstore.com/"
                    val name = productObj.optString("name", "N/A").replace(Regex("[^A-Za-z\\s]"), "").trim()
                    val price = String.format("%.2f", productObj.optDouble("price", 0.0))
                    val rating = productObj.optString("rating", "0")
                    val mainImage = productObj.optString("image", "")
                    val fullImageUrl = if (mainImage.startsWith("http")) mainImage else "$baseUrl/image/$mainImage"

                    val descriptionHtml = productObj.optString("description", "")
                    val doc = Jsoup.parse(descriptionHtml)
                    doc.select("script, style").remove()
                    val formattedText = Html.fromHtml(doc.text(), Html.FROM_HTML_MODE_LEGACY).toString().trim()

                    // --- Parse image array ---
                    val imageList = mutableListOf<RelatedImage>()
                    val imageArray = productObj.optJSONArray("image")
                    Log.d("gettingrelatedimaged", "onResponse: $imageArray")
//                    if (imageArray != null) {
//                        for (i in 0 until imageArray.length()) {
//                            val imgObj = imageArray.getJSONObject(i)
//                            val imageUrl = imgObj.optString("image", "")
//                            val fullUrl = if (imageUrl.startsWith("http")) imageUrl else "$baseUrl/$imageUrl"
//                            imageList.add(fullUrl)
//                        }
//                    }

                    // --- Parse related products ---
                    val relatedArray = productObj.optJSONArray("related_products")
                    val relatedList = mutableListOf<RelatedProduct>()
                    if (relatedArray != null) {
                        for (i in 0 until relatedArray.length()) {
                            val related = relatedArray.getJSONObject(i)
                            val rImg = related.optString("image", "")
                            val rUrl = if (rImg.startsWith("http")) rImg else "$baseUrl/$rImg"
                            relatedList.add(
                                RelatedProduct(
                                    productId = related.optString("product_id", ""),
                                    name = related.optString("name", ""),
                                    price = related.optString("price", ""),
                                    image = rUrl,
                                    description = related.optString("description", ""),
                                    Wished = false
                                )
                            )
                        }
                    }

                    // --- Update UI ---
                    binding.productname.text = name
                    binding.actualprice.text = "$$price"
                    binding.discountprice.text = "$$price"
                    binding.descText.text = formattedText
                    binding.rating.text = "$rating ★"

                    Glide.with(this@ProductDetailPage)
                        .load(fullImageUrl)
                        .placeholder(R.drawable.download)
                        .error(R.drawable.download)
                        .into(binding.productImage)

                    val relatedImages = imageList.map { RelatedImage(
                        image = it.image,
                        productId = productObj.optString("product_id")
                    ) }
                    Log.d("relatedimagesss", "onResponse: $relatedImages")
                    adapterpdp.updateList(relatedImages)

                    relatedAdapter.updateList(relatedList)

                    // --- Store in CartItem ---
                    currentProduct = CartItem(
                        productId = productObj.optString("product_id"),
                        name = name,
                        price = price,
                        imageUrl = fullImageUrl
                    )

                    Log.d("ProductImages", "Fetched ${imageList.size} images")
                    Log.d("RelatedProducts", "Fetched ${relatedList.size} related items")

                } catch (e: Exception) {
                    e("ProductDetailError", "Parsing error: ${e.localizedMessage}")
                    Toast.makeText(this@ProductDetailPage, "Parsing error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ProductDetailPage, "API request failed", Toast.LENGTH_SHORT).show()
                e("ProductDetailError", t.localizedMessage ?: "unknown error")
            }
        })
    }

    private fun updateFavIcon(isFavorite: Boolean) {
        if (isFavorite) {
            binding.favbtn.setImageResource(R.drawable.addedwishlist) // red heart
        } else {
            binding.favbtn.setImageResource(R.drawable.fav)
        }
    }

}
