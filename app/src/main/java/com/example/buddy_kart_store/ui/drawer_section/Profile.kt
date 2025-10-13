package com.example.buddy_kart_store.ui.drawer_section

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.buddy_kart_store.databinding.ActivityProfileBinding
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.ui.Home.MainActivity
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.SessionManager
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPref: Sharedpref


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = Sharedpref(this)

        // Handle back button click
        binding.profilebackButton.setOnClickListener {
            navigateBackToMainWithDrawerOpen()
        }

        // ✅ Load saved user data
//        if (sharedPref.isRegisterd()) {
//            binding.nameInput.setText(sharedPref.prefs.getString("FIRST_NAME", ""))
//            Log.d("running", "onCreate: runnung")
//            binding.emailInput.setText(sharedPref.prefs.getString("LAST_NAME", ""))
//            binding.dobInput.setText(sharedPref.getEmail())
//            binding.phoneInput.setText(sharedPref.prefs.getString("PHONE", ""))
//
//        }
        val allPrefs = sharedPref.prefs.all
        Log.d("ProfileCheck", "All SharedPrefs: $allPrefs")


        if (sharedPref.isRegisterd()||sharedPref.isLoggedIn()) {
            val firstName = sharedPref.prefs.getString("FIRST_NAME", "")
            val lastName = sharedPref.prefs.getString("LAST_NAME", "")
            val email = sharedPref.getEmail()
            val phone = sharedPref.prefs.getString("PHONE", "")

            // Log values to check
            Log.d("ProfileData", "First Name: $firstName")
            Log.d("ProfileData", "Last Name: $lastName")
            Log.d("ProfileData", "Email: $email")
            Log.d("ProfileData", "Phone: $phone")

            // Set the fields
            binding.nameInput.setText(firstName)
            binding.emailInput.setText(lastName)
            binding.dobInput.setText(email)
            binding.phoneInput.setText(phone)
        }


        // Input validation (remove error while typing)
        setupValidation()

        // Save button → trigger API only if valid
        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                updateProfile()
                Log.d("running", "onCreate: clicked")
            }
        }
    }

    private fun setupValidation() {
        binding.nameInput.addTextChangedListener { binding.nameContainer.error = null }
        binding.emailInput.addTextChangedListener { binding.emailContainer.error = null }
        binding.dobInput.addTextChangedListener { binding.dobContainer.error = null }
        binding.phoneInput.addTextChangedListener { binding.phoneContainer.error = null }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val firstname = binding.nameInput.text.toString().trim()
        val lastname = binding.emailInput.text.toString().trim()
        val email = binding.dobInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()



        if (firstname.isEmpty()) {
            binding.nameContainer.error = "First name required"
            isValid = false
        }
        if (lastname.isEmpty()) {
            binding.emailContainer.error = "Last name required"
            isValid = false
        }
        if (email.isEmpty()) {
            binding.dobContainer.error = "Email required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.dobContainer.error = "Invalid email"
            isValid = false
        }
        if (phone.isEmpty()) {
            binding.phoneContainer.error = "Phone number required"
            isValid = false
        } else if (phone.length < 10) {
            binding.phoneContainer.error = "Enter valid phone number"
            isValid = false
        }
        return isValid
    }

    private fun navigateBackToMainWithDrawerOpen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("OPEN_DRAWER", true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateBackToMainWithDrawerOpen()
    }

    // ✅ Profile Update API Call
    private fun updateProfile() {
        val firstname = binding.nameInput.text.toString().trim()
        val lastname = binding.emailInput.text.toString().trim()
        val email = binding.dobInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()

        RetrofitClient.iInstance.getAccountDetails(
            route = "wbapi/wbaccount.getaccountupdate",
            customerId = SessionManager.getCustomerId(this).toString(),
            firstname = firstname,
            lastname = lastname,
            email = email,
            telephone = phone,
            custom_field = { }
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val raw = response.body()!!.string()
                    Log.d("ProfileUpdate", "Raw Response: $raw")

                    if (raw.trim().startsWith("<!DOCTYPE") || raw.trim().startsWith("<html")) {
                        Toast.makeText(
                            this@Profile,
                            "Server Error: Invalid response",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val startIndex = raw.indexOf("{")
                    val endIndex = raw.lastIndexOf("}")
                    if (startIndex != -1 && endIndex != -1) {
                        val cleanJson = raw.substring(startIndex, endIndex + 1)
                        try {
                            val jsonObject = JSONObject(cleanJson)
                            val success = jsonObject.optString("success")

                            if (success == "1" || success.equals("true", true)) {
                                // ✅ Update SharedPref with new values
                                sharedPref.saveUser(
                                    firstname,
                                    lastname,
                                    phone,
                                    email,
                                    sharedPref.getPassword() ?: "",
                                    sharedPref.getCustomerId() ?: ""
                                )
                                // ✅ Navigate back to MainActivity with drawer open
                                navigateBackToMainWithDrawerOpen()
                                Toast.makeText(
                                    this@Profile,
                                    "Profile updated successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val msg = jsonObject.optString("message", "Update failed")
                                Toast.makeText(this@Profile, msg, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("ProfileUpdate", "JSON parse error: ${e.message}")
                        }
                    }
                } else {
                    Toast.makeText(this@Profile, "Error: ${response.code()}", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@Profile, "Network error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}
