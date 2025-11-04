package com.example.buddy_kart_store.ui.Home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.databinding.ActivityProductDetailPageBinding
import com.example.buddy_kart_store.model.repository.CartRepo
import com.example.buddy_kart_store.model.repository.FetchWishListRepo
import com.example.buddy_kart_store.model.repository.RelatedProductRepo
import com.example.buddy_kart_store.model.repository.ReviewRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.CartItem
import com.example.buddy_kart_store.model.retrofit_setup.login.HomeProduct
import com.example.buddy_kart_store.model.retrofit_setup.login.RelatedImage
import com.example.buddy_kart_store.model.retrofit_setup.login.RelatedProduct
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.ui.drawer_section.MyWishlist
import com.example.buddy_kart_store.ui.recyclerviews.FullscreenImageAdapter
import com.example.buddy_kart_store.ui.recyclerviews.ProductReviewRecycler
import com.example.buddy_kart_store.ui.recyclerviews.RelatedImagesPagerAdapter
import com.example.buddy_kart_store.ui.recyclerviews.RelatedProductAdapter
import com.example.buddy_kart_store.ui.viewmodel.ReviewVM
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.ui.viewmodel.relatedProoductVM
import com.example.buddy_kart_store.utils.CartManager
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.GenericViewModelFactory
import com.example.buddy_kart_store.utlis.SessionManager
import jp.wasabeef.blurry.Blurry
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.view.isVisible

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

    private var allImages: List<RelatedImage> = emptyList()

    private lateinit var viewModel: ReviewVM
    private lateinit var addViewModel: WishListVM
    private lateinit var fetchRelatedProducts: relatedProoductVM
    private lateinit var adapter: ProductReviewRecycler
    private lateinit var cartVM: fetchCartVM

    private var productId: String? = null


    @SuppressLint("ClickableViewAccessibility")
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
        val cartRepository = CartRepo(RetrofitClient.iInstance, this)
        val cartFactory = GenericViewModelFactory { fetchCartVM(cartRepository) }
        cartVM = ViewModelProvider(this, cartFactory)[fetchCartVM::class.java]


        // -------------------- Wishlist Button --------------------
        val repository = FetchWishListRepo(RetrofitClient.iInstance)
        val addFactory = GenericViewModelFactory { WishListVM(repository) }
        addViewModel = ViewModelProvider(this, addFactory)[WishListVM::class.java]

// Get productId safely
        productId = intent.getStringExtra(EXTRA_PRODUCT_ID) ?: ""
        if (productId.isNullOrEmpty()) {
            Toast.makeText(this, "No Product ID received", Toast.LENGTH_SHORT).show()
            return
        }

// Check wishlist state from SharedPrefs
        isFavorite = Sharedpref.WishlistPrefs.isInWishlist(this, productId!!)
        updateFavIcon(isFavorite)

        binding.favbtn.setOnClickListener {
            val customerId = SessionManager.getCustomerId(this) ?: return@setOnClickListener

            isFavorite = !isFavorite // toggle state immediately for UI feedback
            updateFavIcon(isFavorite)

            if (isFavorite) {
                // Add product to wishlist
                Sharedpref.WishlistPrefs.addProductId(this, productId!!)
                addViewModel.addToWishlist(productId!!, customerId) { success, message ->
                    if (!success) {
                        // revert if API fails
                        isFavorite = false
                        Sharedpref.WishlistPrefs.removeProductId(this, productId!!)
                        updateFavIcon(false)
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                // Remove from wishlist
                Sharedpref.WishlistPrefs.removeProductId(this, productId!!)
                addViewModel.removeFromWishlist(productId!!, customerId) { success, message ->
                    if (!success) {
                        // revert if API fails
                        isFavorite = true
                        Sharedpref.WishlistPrefs.addProductId(this, productId!!)
                        updateFavIcon(true)
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.wishlist.setOnClickListener {
            val intent = Intent(this, MyWishlist::class.java)
            intent.putExtra("fromActivity", "PDP")
            startActivity(intent)
        }

        addViewModel.wishCountLiveData.observe(this) { count ->
            if (count > 0) {
                binding.wishlistCount.visibility = View.VISIBLE
                val wishcount = count.toString()
                Log.d("gettingwishcount", "onCreate: $wishcount")
                binding.wishlistCount.text = wishcount

            } else {
                binding.wishlistCount.visibility = View.GONE
            }
            val initialWishCount = Sharedpref.WishlistPrefs.getWishlistIds(this).size
            addViewModel.wishCountLiveData.postValue(initialWishCount)


        }

// Initial update (in case data is already there)
        val currentIds = Sharedpref.WishlistPrefs.getWishlistIds(this)
        addViewModel.updateWishCount(currentIds)

        currentProduct?.let { product ->
            updateFavIcon(product.favorite)
        }
        // -------------------- Cart Button --------------------
        binding.cartbtn.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        cartVM.cartCountLiveData.observe(this) { count ->
            if (count > 0) {
                binding.cartCount.visibility = View.VISIBLE
                val wishcount = count.toString()
                binding.cartCount.text = wishcount

            } else {
                binding.cartCount.visibility = View.GONE
            }
            val initialCartCount = Sharedpref.CartPrefs.getCartIds(this).size
            cartVM.cartCountLiveData.postValue(initialCartCount)

            binding.cart.setOnClickListener {
                val productId = "1234" // Example product ID
                cartVM.addProductToCart(productId, this)
            }
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



// Can be a boolean variable in your Activity/Fragment
        var isInCart = Sharedpref.CartPref.isInCart(this, productId.toString())

        binding.addtocartbtn.apply {
            text = if (isInCart) "Go to Cart" else "Add to Cart"

            setOnClickListener {
                if (!isInCart) {
                    isEnabled = false
                    text = "Adding..."

                    cartVM.addToCart(
                        customerId.toString(),
                        SessionManager.getSessionId(this@ProductDetailPage).toString(),
                        productId.toString()
                    ) { success, message ->
                        isEnabled = true
                        if (success) {
                            isInCart = true
                            text = "Go to Cart"

                            // save to SharedPref immediately
                            Sharedpref.CartPref.saveCartMapping(
                                this@ProductDetailPage, productId.toString(),  "someId"
                            )

                            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                        } else {
                            text = "Add to Cart"
                            Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    startActivity(Intent(context, CartActivity::class.java))
                }
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
            viewModel = cartVM
        )
        if(relatedAdapter.itemCount==0){
            binding.noProductText.visibility = View.VISIBLE
            binding.relatedproductadapter.visibility = View.GONE
            binding.relatedProduct.visibility = View.GONE
        }

        binding.relatedproductadapter.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.relatedproductadapter.adapter = relatedAdapter

        fetchRelatedProducts.relatedProducts.observe(this) { products ->
            relatedAdapter.updateList(products)
        }

        productId?.let { fetchRelatedProducts.fetchRelatedProducts(it) }




        adapterpdp = RelatedImagesPagerAdapter(allImages) { position ->
            showImageOverlay(position)
        }
        binding.relatedImagesViewPager.adapter = adapterpdp
        binding.relatedImagesViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        binding.relatedImagesViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val count = binding.imageIndicator.childCount
                for (i in 0 until count) {
                    val dot = binding.imageIndicator.getChildAt(i) as ImageView
                    if (i == position) {
                        dot.setImageResource(R.drawable.dot_active)
                    } else {
                        dot.setImageResource(R.drawable.dot_inactive)
                    }
                }
            }
        })


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


        binding.imageOverlay.setOnTouchListener { v, event ->
            val viewPagerRect = Rect()
            binding.fullscreenViewPager.getGlobalVisibleRect(viewPagerRect)

            if (!viewPagerRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                closeOverlay()
                true
            } else {
                false
            }
        }



    }
    override fun onBackPressed() {
        if (binding.imageOverlay.isVisible) {
            closeOverlay()
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchProductDetails(productId: Int) {
        Log.d("ProductDetailPageprooductdetin", "Fetching product details for ID: $productId")

        RetrofitClient.iInstance.getProductsDetail(
            route = "wbapi/wbproductapi.getproduct",
            productId = productId.toString()
        ).enqueue(object : Callback<ResponseBody> {

            @SuppressLint("DefaultLocale")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful || response.body() == null) {
                    Log.d("gettingresponseforpdp", "onResponse: $response")
                    Toast.makeText(this@ProductDetailPage, "Failed to fetch product", Toast.LENGTH_SHORT).show()
                    return
                }

                try {
                    val jsonString = response.body()!!.string()
                    Log.d("gettingresponseforpdp", "onResponse: $jsonString")
                    val json = JSONObject(jsonString)
                    val data = json.opt("data")

                    // Extract product object safely
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


                    // Product details
                    val name = productObj.optString("name", "N/A").replace(Regex("[^A-Za-z0-9\\s]"), "").trim()
                    val price = String.format("%.2f", productObj.optDouble("price", 0.0))
                    val rating = productObj.optString("rating", "0")
                    val special = String.format("%.2f", productObj.optDouble("special", 0.0))

                    val rawImage = productObj.optString("image", "")
                    val decodedImage = Html.fromHtml(rawImage, Html.FROM_HTML_MODE_LEGACY).toString() // decode &amp;
                    val encodedImage = decodedImage.replace(" ", "%20") // replace spaces
                    val baseUrl = "https://hellobuddy.jkopticals.com/image/"
                    val fullMainImage = if (encodedImage.startsWith("http")) {
                        encodedImage
                    } else {
                        baseUrl + encodedImage
                    }


                    // Parse description
                    val descriptionHtml = productObj.optString("description", "")
                    val doc = Jsoup.parse(descriptionHtml)
                    doc.select("script, style").remove()
                    val formattedText = Html.fromHtml(doc.text(), Html.FROM_HTML_MODE_LEGACY).toString().trim()

                    // Parse related products
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

                    // Parse additional images
                    val imageArray = json.optJSONArray("image") ?: JSONArray()
                    val additionalImages = mutableListOf<RelatedImage>()
                    for (i in 0 until imageArray.length()) {
                        val imgObj = imageArray.getJSONObject(i)
                        val rawImage = imgObj.optString("image", "")
                        val decodedImage = Html.fromHtml(rawImage, Html.FROM_HTML_MODE_LEGACY).toString() // decode &amp; to &
                        val encodedImage = decodedImage.replace(" ", "%20") // encode spaces

                        val baseUrl = "https://hellobuddy.jkopticals.com/image/"
                        val fullMainImage = if (encodedImage.startsWith("http")) {
                            encodedImage
                        } else {
                            baseUrl + encodedImage
                        }
                        additionalImages.add(RelatedImage(image = fullMainImage, productId = productObj.optString("product_id")))
                    }

                    // Combine main + additional images
                    allImages = mutableListOf<RelatedImage>().apply {
                        add(RelatedImage(image = fullMainImage, productId = productObj.optString("product_id")))
                        addAll(additionalImages)
                    }

                    val actual = price
                    val discount = special
                    val disc = ((actual.toDouble() - discount.toDouble()) / actual.toDouble() * 100).toInt()


                    // Update UI
                    binding.productname.text = name
                    binding.discountprice.text = "₹${special}"
                    binding.actualprice.text = "₹${price}"
                    binding.descText.text = formattedText
                    binding.rating.text = "$rating ★"
                    binding.discount.text = "${disc}%OFF"



                    // Update image slider adapter
                    adapterpdp.updateList(allImages)
                    setupIndicators(allImages.size)

                    // Update related products
                    relatedAdapter.updateList(relatedList)

                    // Store current product for cart/wishlist
                    currentProduct = CartItem(
                        productId = productObj.optString("product_id"),
                        name = name,
                        price = price,
                        imageUrl = fullMainImage
                    )

                    Log.d("ProductImages", "Fetched ${allImages.size} images")
                    Log.d("RelatedProducts", "Fetched ${relatedList.size} related items")

                } catch (e: Exception) {
                    Log.e("ProductDetailError", "Parsing error: ${e.localizedMessage}")
                    Toast.makeText(this@ProductDetailPage, "Parsing error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ProductDetailPage, "API request failed", Toast.LENGTH_SHORT).show()
                Log.e("ProductDetailError", t.localizedMessage ?: "unknown error")
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
    private fun setupIndicators(count: Int) {
        val indicatorLayout = binding.imageIndicator
        indicatorLayout.removeAllViews()

        val dots = Array(count) { ImageView(this) }
        for (i in 0 until count) {
            dots[i] = ImageView(this).apply {
                setImageResource(R.drawable.dot_inactive)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(4, 0, 4, 0) // spacing between dots
                layoutParams = params
            }
            indicatorLayout.addView(dots[i])
        }

        // set first dot as active
        if (dots.isNotEmpty()) {
            dots[0].setImageResource(R.drawable.dot_active)

        }
    }

    private fun showImageOverlay(startPosition: Int) {
        // Safety check
        if (allImages.isEmpty()) return

        val overlay = binding.imageOverlay
        val viewPager = binding.fullscreenViewPager
        val indicatorLayout = binding.fullscreenIndicator

        // Blur the background
        Blurry.with(this).radius(15).sampling(2).onto(binding.main)

        // Set up fullscreen adapter with all images
        val adapter = FullscreenImageAdapter(allImages)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(startPosition, false)

        // Set up dot indicators
        indicatorLayout.removeAllViews()
        val dots = Array(allImages.size) { ImageView(this) }
        for (i in dots.indices) {
            dots[i] = ImageView(this).apply {
                setImageResource(if (i == startPosition) R.drawable.dot_active else R.drawable.dot_inactive)
                val params = LinearLayout.LayoutParams(20, 20)
                params.marginStart = 6
                params.marginEnd = 6
                layoutParams = params
            }
            indicatorLayout.addView(dots[i])
        }

        // Update dots on page change
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (i in dots.indices) {
                    dots[i].setImageResource(if (i == position) R.drawable.dot_active else R.drawable.dot_inactive)
                }
            }
        })

        // Animate overlay in
        overlay.alpha = 0f
        overlay.visibility = View.VISIBLE
        overlay.animate().alpha(1f).setDuration(200).start()

        // Dismiss overlay on click
        overlay.setOnClickListener {
            overlay.animate().alpha(0f).setDuration(200).withEndAction {
                overlay.visibility = View.GONE
                Blurry.delete(binding.main)
            }.start()
        }
    }

    private fun closeOverlay() {
        binding.imageOverlay.animate().alpha(0f).setDuration(200).withEndAction {
            binding.imageOverlay.visibility = View.GONE
            Blurry.delete(binding.main)
        }.start()
    }

    override fun onResume() {
        super.onResume()
        fetchProductDetails(productId!!.toInt())
    }


}
