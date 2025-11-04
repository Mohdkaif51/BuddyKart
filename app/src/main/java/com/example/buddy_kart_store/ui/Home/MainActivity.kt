package com.example.buddy_kart_store.ui.Home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.databinding.ActivityMainBinding
import com.example.buddy_kart_store.model.repository.CartRepo
import com.example.buddy_kart_store.model.repository.FetchWishListRepo
import com.example.buddy_kart_store.model.repository.HomeRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.model.retrofit_setup.login.categories
import com.example.buddy_kart_store.ui.drawer_section.AddressMain
import com.example.buddy_kart_store.ui.drawer_section.ChangePassword
import com.example.buddy_kart_store.ui.drawer_section.MyWishlist
import com.example.buddy_kart_store.ui.drawer_section.OrderPage
import com.example.buddy_kart_store.ui.drawer_section.Profile
import com.example.buddy_kart_store.ui.login.SignIn
import com.example.buddy_kart_store.ui.recyclerviews.HomeAdapter
import com.example.buddy_kart_store.ui.recyclerviews.ImageSlider
import com.example.buddy_kart_store.ui.recyclerviews.ImageSliderController
import com.example.buddy_kart_store.ui.viewmodel.WishListVM
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.GenericViewModelFactory
import com.example.buddy_kart_store.utlis.MyApp
import com.example.buddy_kart_store.utlis.SessionManager
import com.example.buddy_kart_store.viewmodel.HomeVm
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var imageSlider: ImageSlider

    private lateinit var sliderController: ImageSliderController
    private lateinit var cartVM: fetchCartVM
    private lateinit var wishlistVM: WishListVM
    private lateinit var shareDb: Sharedpref
    private lateinit var home: HomeVm

    private val categoryList = mutableListOf<categories>()
    private lateinit var topSlider: ImageSlider
    private lateinit var bottomSlider: ImageSlider
//    private lateinit var images: List<String>

    @SuppressLint("NotifyDataSetChanged", "CutPasteId")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        topSlider = ImageSlider(this)
        bottomSlider = ImageSlider(this)

        binding.wishlist.setOnClickListener {
            binding.progressbar.visibility = View.VISIBLE
            val intent = Intent(this, MyWishlist::class.java)
            intent.putExtra("fromActivity", "CategoryPage")
            startActivity(intent)
            binding.progressbar.postDelayed({
                binding.progressbar.visibility = View.GONE
            }, 800)
        }



        binding.cartt.setOnClickListener {
            binding.progressbar.visibility = View.VISIBLE

            startActivity(
                Intent(
                    this, CartActivity
                    ::class.java
                )
            )
            binding.progressbar.postDelayed({
                binding.progressbar.visibility = View.GONE
            }, 1000)

        }

        binding.navigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))

        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView
        binding.searchBar.setOnClickListener {
            startActivity(Intent(this, Search::class.java))
        }



        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }

                R.id.nav_logout -> {
                    showDeleteConfirmationDialog()
                }

                R.id.nav_password -> {
                    startActivity(Intent(this, ChangePassword::class.java))
                }

                R.id.nav_address -> {
                    startActivity(Intent(this, AddressMain::class.java))
                }

                R.id.nav_wishlist -> {
                    startActivity(Intent(this, MyWishlist::class.java))
                }

                R.id.nav_orders -> {
                    startActivity(Intent(this, OrderPage::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        navigationView.getHeaderView(0).findViewById<ImageButton>(R.id.editButton).visibility =
            View.GONE



        binding.sidenavigation.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (drawerView is NavigationView) {
                    val width = resources.displayMetrics.widthPixels
                    val params = drawerView.layoutParams
                    params.width = width
                    drawerView.layoutParams = params
                }
            }

            override fun onDrawerOpened(drawerView: View) {
                setupDrawerBackButton()
            }

            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })

        if (intent.getBooleanExtra("OPEN_DRAWER", false)) {
            drawerLayout.postDelayed({
                drawerLayout.openDrawer(GravityCompat.START)
            }, 100)
        }



        imageSlider = ImageSlider(this)
        imageSlider.setupSlider(mutableListOf()) // empty initially
        imageSlider.startAutoScroll()





        cartVM = fetchCartVM(CartRepo(RetrofitClient.iInstance, this))

        val wishlistFactory =
            GenericViewModelFactory { WishListVM(FetchWishListRepo(RetrofitClient.iInstance)) }
        wishlistVM = ViewModelProvider(this, wishlistFactory)[WishListVM::class.java]

        val homeFactory = GenericViewModelFactory { HomeVm(HomeRepo(RetrofitClient.iInstance)) }
        home = ViewModelProvider(this, homeFactory)[HomeVm::class.java]


        // -------------------------------
        // Setup RecyclerView with Adapter
        // -------------------------------
        val customerId = SessionManager.getCustomerId(this) ?: ""
        val sessionId = SessionManager.getSessionId(this) ?: ""

        val homeAdapter = HomeAdapter(emptyList(), cartVM, wishlistVM)
        cartVM.fetchCart(customerId, sessionId)
        cartVM.cartItems.observe(this) { cartList ->
            val cartIds = cartList.map { it.cart_id }.toSet()
            val quantities = cartList.associate { it.product_id to it.quantity }
//


        }

        binding.homeRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.homeRecyclerView.adapter = homeAdapter
        binding.progressbar.visibility = View.VISIBLE
        homeAdapter


        home.homeModulesLiveData.observe(this) { modules ->
            Log.d("HOME_MODULES", "Received modules: ${modules.size}")
//            binding.progressbar.postDelayed({
//                binding.progressbar.visibility = View.GONE
//            }, 1000)

            homeAdapter.updateList(modules) // Make sure HomeAdapter has updateList()
        }
        binding.progressbar.postDelayed({
            binding.progressbar.visibility = View.GONE
        }, 600)

        home.loadHome()


        binding.swipeRefreshLayout.setOnRefreshListener {

            // Observe home data once
            home.homeModulesLiveData.observe(this) { modules ->
                homeAdapter.updateList(modules)

                // Fade out overlay smoothly
                binding.loadingOverlay.animate()
                    .alpha(0f)
                    .setDuration(400)
                    .withEndAction {
                        binding.loadingOverlay.visibility = View.GONE
                    }
                    .start()

                // Reset RecyclerView alpha back to 1
                binding.homeRecyclerView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()

                // Stop swipe refresh animation
                binding.swipeRefreshLayout.isRefreshing = false
            }

// Swipe to refresh
            binding.swipeRefreshLayout.setOnRefreshListener {
                // Show overlay
                binding.loadingOverlay.alpha = 0f
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.loadingOverlay.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()

                // Fade RecyclerView slightly
                binding.homeRecyclerView.animate()
                    .alpha(0.7f)
                    .setDuration(300)
                    .start()

                // Simulate realistic API delay
                binding.homeRecyclerView.postDelayed({
                    home.loadHome() // trigger API
                }, 1000)
            }


        }
        if (!MyApp.AppPrefs.isHomeApiCalled(this)) {
            val customerId = SessionManager.getCustomerId(this) ?: ""
            val sessionId = SessionManager.getSessionId(this) ?: ""


            cartVM.fetchCart(customerId, sessionId)
            MyApp.AppPrefs.setHomeApiCalled(this) // mark as called

            Log.d("calledapi", "onCreate: api called")
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



        wishlistVM.wishCountLiveData.observe(this) { count ->
            if (count > 0) {
                binding.wishlistCount.visibility = View.VISIBLE
                val wishcount = count.toString()
                binding.wishlistCount.text = wishcount

            } else {
                binding.wishlistCount.visibility = View.GONE
            }
            val initialWishCount = Sharedpref.WishlistPrefs.getWishlistIds(this).size
            wishlistVM.wishCountLiveData.postValue(initialWishCount)


        }
    }



    private fun setupDrawerBackButton() {
        try {
            for (i in 0 until navigationView.headerCount) {
                val headerView = navigationView.getHeaderView(i)


                val backButton = headerView.findViewById<View>(R.id.backButton)
                if (backButton != null) {
                    backButton.setOnClickListener {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    Log.d("MainActivity", "Back button found at header index $i")
                    return
                }
            }
            Log.e("MainActivity", "Back button not found in any header")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in setupDrawerBackButton: ${e.message}")
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("OPEN_DRAWER", false)) {
            drawerLayout.postDelayed({
                drawerLayout.openDrawer(GravityCompat.START)
            }, 100)


        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    //    for slider
    override fun onPause() {
        super.onPause()
        if (::sliderController.isInitialized) sliderController.stopAutoScroll()
    }


    override fun onResume() {
        super.onResume()
        home.loadHome()
        imageSlider.startAutoScroll()
        updateDrawerHeader()
    }

    override fun onDestroy() {
        super.onDestroy()
        imageSlider.cleanup()
        bottomSlider.cleanup()
        topSlider.cleanup()

    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                val sharedPref = Sharedpref(this)
                sharedPref.logout()

                // Redirect to SignIn
                val intent = Intent(this, SignIn::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun updateDrawerHeader() {
        val sharesPref = Sharedpref(this)

        if (sharesPref.isRegisterd()) {
            val header = navigationView.getHeaderView(0)
            header.findViewById<TextView>(R.id.userEmail).text = sharesPref.getEmail()
            header.findViewById<TextView>(R.id.userName).text = sharesPref.getName()
            val firstChar = sharesPref.getName()?.getOrNull(0)?.toString() ?: ""

            header.findViewById<TextView>(R.id.profileText).text = firstChar


        }
    }


}
