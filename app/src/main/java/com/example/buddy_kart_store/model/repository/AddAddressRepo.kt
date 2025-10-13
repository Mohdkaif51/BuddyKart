package com.example.buddy_kart_store.model.repository


import android.util.Log
import com.example.buddy_kart_store.model.retrofit_setup.login.Address
import com.example.buddy_kart_store.model.retrofit_setup.login.Country
import com.example.buddy_kart_store.model.retrofit_setup.login.apiService
import com.example.buddy_kart_store.model.retrofit_setup.login.state
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddAddressRepo(private val apiService: apiService) {

    // Add Address API
    fun addAddress(
        customerId: String,
        firstname: String,
        lastname: String,
        company: String,
        address1: String,
        address2: String,
        city: String,
        postcode: String,
        countryId: String,
        zoneId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        apiService.addAddress(
            route = "wbapi/addaddressapi.adaddress",
            customerId = customerId,
            firstname = firstname,
            lastname = lastname,
            company = company,
            address_1 = address1,
            address_2 = address2,
            city = city,
            postcode = postcode,
            country_id = countryId,
            zone_id = zoneId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val bodyString = response.body()?.string()
                    Log.d("AddAddressRepo", "Raw response: $bodyString")

                    if (!bodyString.isNullOrEmpty()) {
                        val json = JSONObject(bodyString)
                        val success = json.optString("success") == "true"
                        val message = json.optString("message")
                        Log.d(
                            "AddAddressRepo",
                            "Parsed: success=$success, message=$message"
                        ) // 👈 log parsed

                        onResult(success, message)
                    } else {
                        onResult(false, "Empty response from server")
                    }
                } catch (e: Exception) {
                    onResult(false, "Parsing error: ${e.message}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResult(false, t.message ?: "Network error")
            }
        })
    }


    // Fetch countries
    fun fetchCountries(query: String, onResult: (List<Country>) -> Unit) {
        apiService.fetchCountries(
            route = "wbapi/countryapi.getcountry",



            ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful || response.body() == null) {
                    Log.d("apicalling", "onResponse: ${response.body()}")
                    onResult(emptyList())
                    return
                }

                try {
                    val rawJson = response.body()!!.string()
                    Log.d("apicalling", "rawJson: $rawJson")

                    val jsonObject = JSONObject(rawJson)
                    val jsonArray = jsonObject.optJSONArray("data") ?: JSONArray()
                    Log.d("apihitting", "data: $jsonArray ")


                    val list = mutableListOf<Country>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        // Country parse
                        val id = obj.optString("country_id", "")
                        val name = obj.optString("name", "")
                        if (id.isNotEmpty() && name.isNotEmpty()) {
                            list.add(Country(id, name))
                        }

                    }




                    onResult(list)
                    Log.d("Unknownapi", "list: $list")


                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult(emptyList())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                onResult(emptyList())
            }
        })
    }

//    for states

    fun fetchStates(
        countryId: Int,
        query: String,
        onResult: (List<state>) -> Unit
    ) {
        apiService.getstate(
            route = "wbapi/countryapi.getcountry",
            countryId = countryId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {


                val rawJson = response.body()?.string() ?: ""
                val list = mutableListOf<state>()
                try {
                    val jsonObject = JSONObject(rawJson)
                    Log.d("apicalling", "onResponse: $jsonObject")
                    val zonesArray = jsonObject.optJSONArray("zones") ?: JSONArray()

                    for (i in 0 until zonesArray.length()) {
                        val zoneObj = zonesArray.getJSONObject(i)
                        val stateId = zoneObj.optString("zone_id", "")
                        val stateName = zoneObj.optString("name", "")
                        if (stateId.isNotEmpty() && stateName.isNotEmpty()) {
                            list.add(state(stateId, stateName))
                        }
                    }

                    // ✅ Filter by query
                    val filtered = list.filter {
                        it.name.contains(query, ignoreCase = true)
                    }

                    onResult(filtered)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult(emptyList())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResult(emptyList())
            }
        })
    }


//    --------------------------------------

    //fetch address
    fun fetchAddresses(
        customerId: String,
        onResult: (Boolean, String, List<Address>?) -> Unit
    ) {
        apiService.getAllAddress(
            route = "wbapi/addressapi.getaddress",
            customerId = customerId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    val bodyString = response.body()?.string()

                    if (!bodyString.isNullOrEmpty()) {
                        val json = JSONObject(bodyString)
                        val success = json.optString("success") == "true"
                        val message = json.optString("message")

                        val addresses = mutableListOf<Address>()
                        val dataObj =
                            json.optJSONObject("data")   // ✅ JSONObject instead of JSONArray
                        if (dataObj != null) {
                            val keys = dataObj.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                val obj = dataObj.getJSONObject(key)
                                addresses.add(
                                    Address(
                                        customerId = obj.optString("customer_id"),
                                        firstname = obj.optString("firstname"),
                                        lastname = obj.optString("lastname"),
                                        company = obj.optString("company"),
                                        address1 = obj.optString("address_1"),
                                        address2 = obj.optString("address_2"),
                                        city = obj.optString("city"),
                                        postcode = obj.optString("postcode"),
                                        country = obj.optString("country"),
                                        zone = obj.optString("zone"),
                                        countryId = obj.optString("country_id"),
                                        zoneId = obj.optString("zone_id"),
                                        addressId = obj.optString("address_id")
                                    )
                                )
                            }
                        }

                        onResult(success, message, addresses)
                    } else {
                        onResult(false, "Empty response", null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult(false, "Parsing error: ${e.message}", null)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResult(false, t.message ?: "Network error", null)
            }
        })
    }

    //deleteAddress
    fun deleteAddress(addressId: String, customerId: String,onResult: (Boolean, String) -> Unit) {
        apiService.deleteAddress(
            route = "wbapi/wbaddressdel.addressDelete",
            addressId = addressId,
            customerId = customerId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val bodyString = response.body()?.string()
                    Log.d("tagggg", "Raw response: $bodyString")

                    if (!bodyString.isNullOrEmpty() && bodyString.trim().startsWith("{")) {
                        val json = JSONObject(bodyString)
                        val success = json.optString("success") == "true"
                        val message = json.optString("message")
                        onResult(success, message)
                    } else {
                        onResult(false, "Invalid response: $bodyString")
                    }
                } catch (e: Exception) {
                    onResult(false, "Parsing error: ${e.message}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResult(false, t.message ?: "Network error")
            }
        })
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
        zoneId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        apiService.updateAddress(
            route = "wbapi/updateaddressapi.updateaddress",
            addressId = addressId,
            customerId = customerId,
            firstname = firstname,
            lastname = lastname,
            company = company,
            address_1 = address1,
            address_2 = address2,
            city = city,
            postcode = postcode,
            country_id = countryId,
            zone_id = zoneId
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val bodyString = response.body()?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        Log.d("UpdateAddress", "Raw response: $bodyString")
                        val json = JSONObject(bodyString)
                        val success = json.optString("success") == "true"
                        val message = json.optString("message")
                        onResult(success, message)
                    } else {
                        onResult(false, "Empty response from server")
                    }
                } catch (e: Exception) {
                    onResult(false, "Parsing error: ${e.message}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResult(false, t.message ?: "Network error")
            }
        })
    }



}