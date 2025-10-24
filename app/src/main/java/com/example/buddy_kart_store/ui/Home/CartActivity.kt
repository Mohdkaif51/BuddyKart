package com.example.buddy_kart_store.ui.Home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buddy_kart_store.databinding.ActivityCartBinding
import com.example.buddy_kart_store.model.repository.CartRepo
import com.example.buddy_kart_store.model.repository.HomeRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.CartDetail
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.ui.recyclerviews.CartRecyclerView
import com.example.buddy_kart_store.ui.viewmodel.fetchCartVM
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.GenericViewModelFactory
import com.example.buddy_kart_store.utlis.SessionManager
import com.example.buddy_kart_store.viewmodel.HomeVm

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartRecyclerView
    private val cartList = mutableListOf<CartDetail>()
    private lateinit var viewModel: fetchCartVM
    private lateinit var home : HomeVm


    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = CartRepo(RetrofitClient.iInstance , this)
        val factory = GenericViewModelFactory { fetchCartVM(repository) }
        viewModel = ViewModelProvider(this, factory)[fetchCartVM::class.java]


        // Observe loader state
        viewModel.isLoading.observe(this) { loading ->
            if (loading) {
                binding.progressbar.visibility = View.VISIBLE  // Show loader
                binding.cartRecyclerView.visibility = View.GONE
            } else {
                binding.progressbar.visibility = View.GONE     // Hide loader
                binding.cartRecyclerView.visibility = View.VISIBLE
            }
        }


        binding.back.setOnClickListener {
            onBackPressed()

        }

        cartAdapter = CartRecyclerView(
            cartList, object : CartRecyclerView.CartItemListener {
                 fun onQuantityChanged(cartItem: CartDetail, newQuantity: Int) {
                    // Call your ViewModel to update cart quantity on server
//                viewModel.updateCartQuantity(cartItem.cart_id, newQuantity)
                }

                override fun onItemDeleted(cartItem: CartDetail) {
                    // Call your ViewModel to delete the item from server cart
//                viewModel.deleteCartItem(cartItem.cart_id)
                }
            },
            viewModel
        )
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this , LinearLayoutManager.VERTICAL, false)

        binding.cartRecyclerView.adapter = cartAdapter

        viewModel.cartItems.observe(this) { cartList ->
            if (cartList.isNotEmpty()) {
                // Cart has items → show pricing and checkout
                binding.pricingCard.visibility = View.VISIBLE
                binding.checkoutcard.visibility = View.VISIBLE
                binding.clearcart.visibility = View.VISIBLE
                binding.noproductimg.visibility = View.GONE

            } else {
                // Cart is empty → hide pricing and checkout
                binding.pricingCard.visibility = View.GONE
                binding.checkoutcard.visibility = View.GONE
                binding.clearcart.visibility = View.GONE
                binding.noproductimg.visibility = View.VISIBLE
            }

            // Update RecyclerView
//            cartAdapter.submitList(cartList)
        }




        val homeFactory = GenericViewModelFactory { HomeVm(HomeRepo(RetrofitClient.iInstance)) }
        home = ViewModelProvider(this, homeFactory)[HomeVm::class.java]



        val sessionid = SessionManager.getSessionId(this).toString()
        val customerId = SessionManager.getCustomerId(this).toString()
        Log.d("sessionid", "onCreate: $sessionid")

        viewModel.fetchCart(customerId, sessionid)
        Log.d("fetchcart", "onCreate: running")

        viewModel.billingAmt.observe(this) { billing ->
            binding.subtotalText.text = "₹${billing.subTotal}"
            binding.taxText.text = "₹${billing.tax}"
            binding.totalAmountText.text = "₹${billing.total}"
            binding.totalTextAmt.text = "₹${billing.total}"

        }


        viewModel.cartItems.observe(this) { items ->
            cartList.clear()
//            home.loadHome()
            cartList.addAll(items)
            cartAdapter.notifyDataSetChanged()
        }

        binding.clearcart.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear Cart")
                .setMessage("Are you sure you want to clear your cart?")
                .setPositiveButton("Yes") { dialog, _ ->

                    // ✅ Step 1: Disable the button temporarily to prevent double clicks
                    binding.clearcart.isEnabled = false

                    // ✅ Step 2: Trigger ViewModel to delete cart items from server
                    viewModel.deleteCart(customerId, sessionid)
                    Sharedpref.CartPref.clearCart(this)



                    // ✅ Step 3: Clear local storage and UI
                    Sharedpref.CartPrefs.clearCart(this)
                    cartList.clear()
                    cartAdapter.notifyDataSetChanged()

                    // ✅ Step 4: Refresh home only if necessary
                    home.loadHome()

                    // ✅ Step 5: Refresh from API again only if backend affects it
                    // Avoid redundant call if deleteCart() already refreshes LiveData
                    viewModel.fetchCart(customerId, sessionid)

                    // ✅ Step 6: Re-enable after short delay or API callback
                    binding.clearcart.postDelayed({ binding.clearcart.isEnabled = true }, 1000)

                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }


    }

}
