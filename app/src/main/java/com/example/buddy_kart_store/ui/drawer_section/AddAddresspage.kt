package com.example.buddy_kart_store.ui.drawer_section

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.databinding.ActivityAddAddresspageBinding
import com.example.buddy_kart_store.model.repository.AddAddressRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.Address
import com.example.buddy_kart_store.model.retrofit_setup.login.Country
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient.iInstance
import com.example.buddy_kart_store.model.retrofit_setup.login.state
import com.example.buddy_kart_store.ui.viewmodel.AddAddressVM
import com.example.buddy_kart_store.utlis.GenericViewModelFactory
import com.example.buddy_kart_store.utlis.SessionManager
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddAddresspage : AppCompatActivity() {

    private lateinit var binding: ActivityAddAddresspageBinding
    private lateinit var viewModel: AddAddressVM

    private var countrySelected = false
    private var stateSelected = false
    private var countryJob: Job? = null
    private var stateJob: Job? = null
    private var selectedCountryId: String? = null
    private var selectedStateId: String? = null

    private var editingAddress: Address? = null // ✅ Detect edit mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAddresspageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener { onBackPressed() }

        val repository = AddAddressRepo(iInstance)
        val factory = GenericViewModelFactory { AddAddressVM(repository) }
        viewModel = ViewModelProvider(this, factory)[AddAddressVM::class.java]

        // Receive address from intent (edit mode)
        editingAddress = intent.getParcelableExtra("address")
        editingAddress?.let { address ->
            binding.etFirstName.setText(address.firstname)
            binding.etLastName.setText(address.lastname)
            binding.etCompany.setText(address.company)
            binding.etAddress1.setText(address.address1)
            binding.etAddress2.setText(address.address2)
            binding.etCity.setText(address.city)
            binding.etPincode.setText(address.postcode)
            binding.etCountry.setText(address.country)  // Display name
            binding.etState.setText(address.zone)       // Display name

            // Internal IDs (must exist in Address model)
            selectedCountryId = address.countryId      // ✅ ID as String
            selectedStateId = address.zoneId          // ✅ ID as String

            binding.btnSaveAddress.text = "Update Address"
        }

        binding.btnSaveAddress.setOnClickListener {
            if (validateFields()) {
                val customerId = SessionManager.getCustomerId(this) ?: ""

                if (editingAddress != null) {
                    // Update mode
                    viewModel.updateAddress(
                        addressId = editingAddress!!.addressId,
                        customerId = customerId,
                        firstname = binding.etFirstName.text.toString().trim(),
                        lastname = binding.etLastName.text.toString().trim(),
                        company = binding.etCompany.text.toString().trim(),
                        address1 = binding.etAddress1.text.toString().trim(),
                        address2 = binding.etAddress2.text.toString().trim(),
                        city = binding.etCity.text.toString().trim(),
                        postcode = binding.etPincode.text.toString().trim(),
                        countryId = selectedCountryId!!,
                        zoneId = selectedStateId!!
                    )
                    Toast.makeText(this, "Address updated successfully", Toast.LENGTH_SHORT).show()


                } else {
                    // Add mode
                    viewModel.addAddress(
                        customerId = customerId,
                        firstname = binding.etFirstName.text.toString().trim(),
                        lastname = binding.etLastName.text.toString().trim(),
                        company = binding.etCompany.text.toString().trim(),
                        address1 = binding.etAddress1.text.toString().trim(),
                        address2 = binding.etAddress2.text.toString().trim(),
                        city = binding.etCity.text.toString().trim(),
                        postcode = binding.etPincode.text.toString().trim(),
                        countryId = selectedCountryId!!,
                        zoneId = selectedStateId!!
                    )
                    Toast.makeText(this, "Address added successfully", Toast.LENGTH_SHORT).show()


                }
                val intent = Intent(this, AddressMain::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

            }
        }

        // Country typing
        binding.etCountry.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (countrySelected) return@addTextChangedListener

            countryJob?.cancel()
            if (query.length >= 2) {
                countryJob = lifecycleScope.launch {
                    delay(300)
                    viewModel.fetchCountries(query)
                }
            }
        }

        // State typing
        binding.etState.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (stateSelected) return@addTextChangedListener

            stateJob?.cancel()
            if (query.length >= 2 && !selectedCountryId.isNullOrEmpty()) {
                stateJob = lifecycleScope.launch {
                    delay(300)
                    viewModel.fetchStates(selectedCountryId!!.toInt(), query)
                }
            }
        }

        // Observe countries
        viewModel.countries.observe(this) { list ->
            val query = binding.etCountry.text.toString().trim()

            // Skip showing dropdown if country already selected
            if (countrySelected) return@observe

            val filtered = list.filter { it.name.contains(query, ignoreCase = true) }
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                filtered
            )
            binding.etCountry.setAdapter(adapter)

            if (query.isNotEmpty()) {
                binding.etCountry.showDropDown()
            }

            binding.etCountry.setOnItemClickListener { _, _, pos, _ ->
                val country: Country? = adapter.getItem(pos)
                country?.let {
                    selectedCountryId = it.id
                    binding.etCountry.setText(it.name, false)
                    countrySelected = true
                    // dropdown will auto close, no need to call showDropDown()
                }
            }
        }

        // Observe states
        viewModel.states.observe(this) { list ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, list)
            if (stateSelected) return@observe

            binding.etState.setAdapter(adapter)
            binding.etState.showDropDown()
            binding.etState.setOnItemClickListener { _, _, pos, _ ->
                val state = adapter.getItem(pos) as state
                selectedStateId = state.id
                binding.etState.setText(state.name, false)
                stateSelected = true
            }
        }
    }


    private fun addTextWatcher(layout: TextInputLayout, editText: AppCompatEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layout.error = null
                layout.isEndIconVisible = true
                layout.boxStrokeColor = getColor(R.color.primaryYellow)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validateFields(): Boolean {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val company = binding.etCompany.text.toString().trim()
        val addressLine1 = binding.etAddress1.text.toString().trim()
        val addressLine2 = binding.etAddress2.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val zipCode = binding.etPincode.text.toString().trim()
        val country = binding.etCountry.text.toString().trim()
        val state = binding.etState.text.toString().trim()

        when {
            firstName.isEmpty() -> {
                binding.etFirstName.error = "Please enter first name"; return false
            }

            lastName.isEmpty() -> {
                binding.etLastName.error = "Please enter last name"; return false
            }


            addressLine1.isEmpty() -> {
                binding.etAddress1.error = "Please enter address line 1"; return false
            }

            city.isEmpty() -> {
                binding.etCity.error = "Please enter city"; return false
            }

            state.isEmpty() -> {
                binding.etState.error = "Please select state"; return false
            }

            country.isEmpty() -> {
                binding.country.error = "Please select country"; return false
            }


            zipCode.isEmpty() -> {
                binding.etPincode.error = "Please enter pin code"; return false
            }

            else -> return true
        }
    }
}
