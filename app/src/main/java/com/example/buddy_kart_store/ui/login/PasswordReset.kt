package com.example.buddy_kart_store.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.buddy_kart_store.databinding.ActivityPasswordResetBinding
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.utlis.SessionManager
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PasswordReset : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordResetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordResetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // go back to SignIn
        binding.btnlogin.setOnClickListener {
            startActivity(Intent(this, SignIn::class.java))
        }

        // Reset password with API
        binding.btnReset.setOnClickListener {
            val email = binding.email.text.toString().trim()

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailContainer.error = "Please enter a valid email"
                return@setOnClickListener
            }
            binding.emailContainer.error = null

            RetrofitClient.iInstance.requestForgetPassword(

                route = "wbapi/wbforgetpass.getforgetpass",
                customerId = SessionManager.getCustomerId(this).toString(),
                email = email
            )
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        val responseBody = response.body()?.string()
                        Log.d("FORGOT_PASS_RESPONSE", "Response: $responseBody")

                        // Check for a successful HTTP status code first
                        if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                            Toast.makeText(
                                this@PasswordReset,
                                "Server returned an invalid response",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        try {
                            val jsonObject = JSONObject(responseBody)
                            val success = jsonObject.optBoolean("success", false)
                            val message = jsonObject.optString("message", "Something went wrong")

                            if (success) {
                                Toast.makeText(this@PasswordReset, message, Toast.LENGTH_SHORT)
                                    .show()
                                // Optionally, you can navigate back after a successful message
                                startActivity(Intent(this@PasswordReset, SignIn::class.java))
                                finish()
                            } else {
                                // Handle the case where the server returns a failure JSON
                                Toast.makeText(this@PasswordReset, message, Toast.LENGTH_SHORT)
                                    .show()
                            }

                        } catch (e: Exception) {
                            Log.e("FORGOT_PASS_ERROR", "Parsing error", e)
                            Toast.makeText(
                                this@PasswordReset,
                                "Error parsing server response",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(
                            this@PasswordReset,
                            "Error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

        }
    }
}
