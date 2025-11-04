package com.example.buddy_kart_store.ui.drawer_section

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buddy_kart_store.databinding.ActivityAddaddressmainBinding
import com.example.buddy_kart_store.model.repository.AddAddressRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.ui.Home.MainActivity
import com.example.buddy_kart_store.ui.recyclerviews.AddressAdapter
import com.example.buddy_kart_store.ui.viewmodel.AddAddressVM
import com.example.buddy_kart_store.utlis.GenericViewModelFactory
import com.example.buddy_kart_store.utlis.SessionManager

class AddressMain : AppCompatActivity() {

    private lateinit var binding: ActivityAddaddressmainBinding
    private lateinit var viewModel: AddAddressVM
    private lateinit var adapter: AddressAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddaddressmainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize repository and ViewModel
        val repository = AddAddressRepo(RetrofitClient.iInstance)
        val factory = GenericViewModelFactory { AddAddressVM(repository) }
        viewModel = ViewModelProvider(this, factory)[AddAddressVM::class.java]


        // Setup RecyclerView
        adapter = AddressAdapter( mutableListOf(), onDeleteClick = { address ->
                viewModel.deleteAddress(
                    customerId = SessionManager.getCustomerId(this) ?: "",
                    addressId = address.addressId
                )

            },
            onEditClick = { address ->
                val intent = Intent(this, AddAddresspage::class.java)
                intent.putExtra("address", address.customerId)
                startActivity(intent)
            }
        )
        // Observe LiveData
        viewModel.addresses.observe(this) { list ->
            adapter.updateList(list)

            if (list.isNullOrEmpty()) {
                binding.noaddress.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.noaddress.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }





        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter

//        if(adapter.itemCount == 0){
//            binding.noaddress.visibility = View.VISIBLE
//            binding.recyclerView.visibility = View.GONE
//        }else{
//            binding.noaddress.visibility = View.GONE
//            binding.recyclerView.visibility = View.VISIBLE
//        }


        // Get customerId
        val customerId = SessionManager.getCustomerId(this) ?: ""

        // Fetch addresses
        viewModel.fetchAddresses(customerId)

        // Observe LiveData
        viewModel.addresses.observe(this) { list ->
            adapter.updateList(list)

        }


        // Back button
        binding.back.setOnClickListener {
            navigateBackToMainWithDrawerOpen()
        }

        // Add new address
        binding.addaddress.setOnClickListener {
            startActivity(Intent(this, AddAddresspage::class.java))
        }
    }

    private fun navigateBackToMainWithDrawerOpen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("OPEN_DRAWER", true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        val customerId = SessionManager.getCustomerId(this) ?: ""
        viewModel.fetchAddresses(customerId) // re-fetch every time screen is visible
    }


}
