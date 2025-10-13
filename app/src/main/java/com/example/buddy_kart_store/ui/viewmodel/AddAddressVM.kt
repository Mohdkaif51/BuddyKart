package com.example.buddy_kart_store.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.buddy_kart_store.model.repository.AddAddressRepo
import com.example.buddy_kart_store.model.retrofit_setup.login.Address
import com.example.buddy_kart_store.model.retrofit_setup.login.Country
import com.example.buddy_kart_store.model.retrofit_setup.login.state

class AddAddressVM(private val repository: AddAddressRepo) : ViewModel() {

    private val _countries = MutableLiveData<List<Country>>()
    val countries: LiveData<List<Country>> get() = _countries

    private val _states = MutableLiveData<List<state>>()
    val states: LiveData<List<state>> get() = _states

    private val _addressResult = MutableLiveData<Pair<Boolean, String>>()
    val addressResult: LiveData<Pair<Boolean, String>> get() = _addressResult


    // 1️⃣ Declare MutableLiveData first
    private val _addresses = MutableLiveData<List<Address>>()
    val addresses: LiveData<List<Address>> get() = _addresses


    fun fetchCountries(query: String) {
        if (query.length < 2) return
        repository.fetchCountries(query) { list ->
            _countries.postValue(list)  // list is List<Country>
        }
    }


    fun fetchStates(countryId: Int, query: String) {
        repository.fetchStates(countryId, query) {
            _states.postValue(it)
        }
    }


    fun addAddress(
        customerId: String,
        firstname: String,
        lastname: String,
        company: String,
        address1: String,
        address2: String,
        city: String,
        postcode: String,
        countryId: String,   // ✅ already ID from Activity
        zoneId: String       // ✅ already ID from Activity
    ) {
        repository.addAddress(
            customerId = customerId,
            firstname,
            lastname,
            company,
            address1,
            address2,
            city,
            postcode,
            countryId,
            zoneId
        ) { success, message ->
            _addressResult.postValue(Pair(success, message))
        }
    }

    fun fetchAddresses(customerId: String) {
        repository.fetchAddresses(customerId) { success, message, data ->
            if (success && data != null) {
                _addresses.postValue(data) // ✅ Use _addresses
            } else {
                Log.e("AddressViewModel", "Failed: $message")
            }
        }
    }


    fun deleteAddress(addressId: String , customerId: String) {
        repository.deleteAddress(
            addressId,
            customerId
        ) { success, message ->
            if (success) {
                _addresses.value = _addresses.value?.filter { it.addressId != addressId }
            } else {
                Log.e("AddressViewModel", "Failed: $message")
            }
        }
    }



    fun updateAddress(
        addressId: String,
        customerId: String,
        firstname: String,
        lastname: String,
        company: String,
        address1: String,
        address2: String,
        city: String,
        postcode: String,
        countryId: String,
        zoneId: String
    ) {
        repository.updateAddress(
            addressId = addressId,
            customerId = customerId,
            firstname = firstname,
            lastname = lastname,
            company = company,
            address1 = address1,
            address2 = address2,
            city = city,
            postcode = postcode,
            countryId = countryId,
            zoneId = zoneId,
            onResult = { success, message ->
                if (success) {

                    _addressResult.postValue(Pair(success, message))
                    fetchAddresses(customerId)
                    Log.d("AddAddressVM", "Update successful: $message")
                } else {
                    Log.e("AddAddressVM", "Update failed: $message")
                }
            }
        )
    }

}

