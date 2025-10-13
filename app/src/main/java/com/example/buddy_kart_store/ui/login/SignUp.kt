package com.example.buddy_kart_store.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.buddy_kart_store.databinding.ActivitySignUpBinding
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.ui.Home.MainActivity
import com.example.buddy_kart_store.utils.Sharedpref
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    // Flags to track success
    private var firebaseSuccess = false
    private var backendSuccess = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.progressbar.visibility = android.view.View.GONE

        // Go to SignIn
        binding.signup.setOnClickListener {
            binding.progressbar.visibility = android.view.View.VISIBLE
            startActivity(Intent(this, SignIn::class.java))
            binding.progressbar.visibility = android.view.View.GONE

        }

        // Clear error when typing
        clearErrorOnTyping(binding.signupEmail, binding.signupEmailContainer)
        clearErrorOnTyping(binding.pass, binding.signinPasswordContainer)
        clearErrorOnTyping(binding.confpass, binding.confirmpasswordContainer)
        clearErrorOnTyping(binding.signupFirstname, binding.signupfirstnamecontainer)
        clearErrorOnTyping(binding.signupLastname, binding.signuplastnamecontainer)
        clearErrorOnTyping(binding.signupPhone, binding.signupPhoneContainer)

        setUpClickListeners()
        setupPhoneValidation()

    }

    private fun setupPhoneValidation() {
        binding.signupPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val mobile = s.toString().trim()
                if (mobile.length < 10) {
                    binding.signupPhone.error = "Enter valid 10 digit Mobile Number"
                } else {
                    binding.signupPhone.error = null
//                    binding.signupMobileContainer.isErrorEnabled = false

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun setUpClickListeners() {
        binding.signupbtn.setOnClickListener {
            if (!binding.termsCheckbox.isChecked) {
                Toast.makeText(this, "Please accept Terms & Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (validateFields()) {
                val firstName = binding.signupFirstname.text.toString()
                val lastName = binding.signupLastname.text.toString()
                val email = binding.signupEmail.text.toString()
                val password = binding.pass.text.toString()
                val phone = binding.signupPhone.text.toString()


                binding.progressbar.visibility = android.view.View.VISIBLE


                // Reset flags
                firebaseSuccess = false
                backendSuccess = false

                // Call both Firebase and backend
                createFirebaseUser(email, password)
                registerBackendUser(firstName, lastName, email, password, phone)
                
            }
        }
    }

    private fun validateFields(): Boolean {
        val firstName = binding.signupFirstname.text.toString().trim()
        val lastName = binding.signupLastname.text.toString().trim()
        val email = binding.signupEmail.text.toString().trim()
        val password = binding.pass.text.toString().trim()
        val phone = binding.signupPhone.text.toString().trim()
        val confirmPassword = binding.confpass.text.toString().trim()

        var isValid = true

        if (firstName.isEmpty()) {
            setError(binding.signupfirstnamecontainer, "Enter First Name")
            isValid = false

        }
        if (lastName.isEmpty()) {
            setError(binding.signuplastnamecontainer, "Enter Last Name")
            isValid = false
        }


        if (email.isEmpty()) {
            setError(binding.signupEmailContainer, "Enter Email")
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError(binding.signupEmailContainer, "Enter valid Email")
            isValid = false
        }

        if (password.isEmpty()) {
            setError(binding.signinPasswordContainer, "Enter Password")
            isValid = false
        } else if (password.length < 6) {
            setError(binding.signinPasswordContainer, "Password must be at least 6 characters")
            isValid = false
        }
        if (phone.isEmpty()) {
            setError(binding.signupPhoneContainer, "Enter Phone Number")
            isValid = false
        } else if (phone.length != 10) {
            setError(binding.signupPhoneContainer, "Enter valid Phone Number")
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            setError(binding.confirmpasswordContainer, "Confirm your Password")
            isValid = false
        } else if (password != confirmPassword) {
            setError(binding.confirmpasswordContainer, "Passwords do not match")
            isValid = false
        }

        return isValid
    }

    private fun setError(textInputLayout: TextInputLayout, message: String) {
        textInputLayout.isErrorEnabled = true
        textInputLayout.error = message
    }

    private fun clearErrorOnTyping(editText: TextInputEditText, textInputLayout: TextInputLayout) {
        editText.addTextChangedListener {
            textInputLayout.isErrorEnabled = false
            textInputLayout.error = null
        }
    }

    // Firebase signup
    private fun createFirebaseUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseSuccess = true
                    checkBothSuccess(email, password)
                } else {
                    Snackbar.make(
                        binding.root,
                        " ${task.exception?.message}",
                        Snackbar.LENGTH_LONG
                    ).setAction("Dismiss") {
                        // Optional: kuch aur karna ho
                    }.show()
                }
                binding.progressbar.visibility = android.view.View.GONE

            }
    }

    // Backend API signup
    private fun registerBackendUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phone: String
    ) {
        RetrofitClient.iInstance.registerUser(
            route = "wbapi/wbregister.register",
            customerId = "",
            firstname = firstName,
            lastname = lastName,
            email = email,
            password = password,
            telephone = phone
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                binding.progressbar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    val respString = response.body()?.string()
                    Log.d("BackendResp", "Raw Response: $respString")

                    try {
                        // ✅ Sirf JSON part nikaalo
                        val cleanJson = respString?.substringAfter("{")?.substringBeforeLast("}")
                            ?.let { "{$it}" }
                        Log.d("BackendResp", "Clean JSON: $cleanJson")

                        val jsonObject = JSONObject(cleanJson ?: "{}")
                        val success = jsonObject.optString("success")

                        if (success == "true" || success == "1") {
                            val customerInfo = jsonObject.optJSONObject("customer_info")
                            val customerId = customerInfo?.optString("customer_id") ?: ""
                            Log.d("customeriddd", "onResponse: $customerId")

                            backendSuccess = true
                            val sharedPref = Sharedpref(applicationContext)
                            sharedPref.saveUser(
                                firstName,
                                lastName,
                                phone,
                                email,
                                password,
                                customerId
                            )

                            // ✅ Log saved prefs to verify
                            val allPrefs = sharedPref.prefs.all
                            Log.d("SharedPrefCheck", "All prefs after save: $allPrefs")

                            checkBothSuccess(email, password)
                        } else {
                            val message =
                                jsonObject.optString("message", "Backend Registration Failed")
                            Toast.makeText(this@SignUp, message, Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@SignUp, "Parse error: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }


                } else {
                    // ❌ API error response
                    val errorBody = response.errorBody()?.string()
                    Log.e("BackendResp", "Error Body: $errorBody")
                    Toast.makeText(
                        this@SignUp,
                        "Backend Failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                binding.progressbar.visibility = android.view.View.GONE

                Toast.makeText(this@SignUp, "Backend Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun checkBothSuccess(email: String, password: String) {
        if (firebaseSuccess && backendSuccess) {
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
