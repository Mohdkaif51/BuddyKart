package com.example.buddy_kart_store.ui.drawer_section

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.databinding.ActivityChangePasswordBinding
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.ui.Home.MainActivity
import com.example.buddy_kart_store.utlis.SessionManager
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePassword : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding

    private var customerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backbtn.setOnClickListener { onBackPressed() }

        // Add text change listeners to restore normal state when user types
        addTextWatcher(binding.passwordContainer, binding.password)
        addTextWatcher(binding.confirmpassword, binding.confirmpass)

        binding.btnReset.setOnClickListener {
            validatePasswords()
        }
        binding.backbtn.setOnClickListener {
            navigateBackToMainWithDrawerOpen()
        }
    }

    private fun navigateBackToMainWithDrawerOpen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("OPEN_DRAWER", true)

        // Prevent multiple MainActivity instances
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        startActivity(intent)
        finish()
    }


    private fun addTextWatcher(
        layout: TextInputLayout,
        editText: androidx.appcompat.widget.AppCompatEditText
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Restore normal state when typing
                layout.error = null
                layout.isEndIconVisible = true
                layout.boxStrokeColor = getColor(R.color.primaryYellow) // normal border color
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showError(layout: TextInputLayout, message: String) {
        layout.error = message
        layout.isEndIconVisible = false
        layout.boxStrokeColor = getColor(R.color.colorError) // red border
    }

    private fun validatePasswords() {
        val newPassword = binding.password.text.toString().trim()
        val confirmPassword = binding.confirmpass.text.toString().trim()

        Log.d("password", "validatePasswords: ${confirmPassword}")

        when {
            newPassword.isEmpty() -> {
                showError(binding.passwordContainer, "Please enter new password")
                return
            }

            newPassword.length < 4 || newPassword.length > 40 -> {
                showError(binding.passwordContainer, "Password must be 4–40 characters")
                return
            }

            confirmPassword.isEmpty() -> {
                showError(binding.confirmpassword, "Please confirm password")
                return
            }

            newPassword != confirmPassword -> {
                showError(binding.confirmpassword, "Passwords do not match")
                return
            }

            else -> {
                // ✅ All validations passed
                binding.passwordContainer.error = null
                binding.passwordContainer.isEndIconVisible = true
                binding.confirmpassword.error = null
                binding.confirmpassword.isEndIconVisible = true

                // Get customerId from SharedPref
                customerId = SessionManager.getCustomerId(this) ?: ""

                Log.d("ChangePass", "Loaded CustomerId: $customerId")

                if (customerId.isNotEmpty()) {
                    changePasswordApiCall(customerId, newPassword, confirmPassword)
                } else {
                    Toast.makeText(this, "Customer ID not found!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun changePasswordApiCall(
        customerId: String,
        newPassword: String,
        confirmPassword: String
    ) {
        RetrofitClient.iInstance.changePassword(
            route = "wbapi/passwordchange.change",
            customerId = customerId,
            newPassword = newPassword,
            confirmPassword = confirmPassword
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val respString = response.body()?.string()
                    Log.d("ChangePasswordResp", "Backend Response: $respString")

                    try {
                        val json = JSONObject(respString)
                        val status = json.optString("status")   // Backend me "status" field hai
                        val message = json.optString("message")

                        if (status == "success") {
                            // Backend update successful, now update Firebase
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                user.updatePassword(newPassword)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                this@ChangePassword,
                                                "Password changed successfully in backend & Firebase!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finish() // Close activity after both updates
                                        } else {
                                            if (task.exception is FirebaseAuthRecentLoginRequiredException) {
                                                // Firebase requires recent login
                                                Toast.makeText(
                                                    this@ChangePassword,
                                                    message,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                finish()
                                            } else {
                                                Toast.makeText(
                                                    this@ChangePassword,
                                                    message,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                            // Do not finish immediately; allow user to read the message
                                        }
                                    }
                            } else {
                                // User not logged in in Firebase
                                Toast.makeText(
                                    this@ChangePassword,
                                    "Password changed in backend. Firebase user not found!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Backend returned error
                            Toast.makeText(this@ChangePassword, message, Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@ChangePassword,
                            "Parse error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("ChangePasswordError", "Parsing exception", e)
                    }
                } else {
                    Toast.makeText(
                        this@ChangePassword,
                        "Failed to change password: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    this@ChangePassword,
                    "Network Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


}
