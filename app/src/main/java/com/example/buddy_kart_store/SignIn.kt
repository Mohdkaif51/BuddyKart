package com.example.buddy_kart_store

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.buddy_kart_store.databinding.ActivitySignInBinding
import com.google.android.material.textfield.TextInputLayout

class SignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle Sign In button click
        binding.signinbtn.setOnClickListener {
            if (validateInputs()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Optional: Finish the current activity to prevent going back to it
                // If valid, proceed
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
            }
        }
        binding.signin.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        // Remove error when user starts typing
        addTextWatcher(binding.name, binding.nameContainer)
        addTextWatcher(binding.pass, binding.passwordContainer)
    }

    private fun validateInputs(): Boolean {
        val email = binding.name.text.toString().trim()
        val password = binding.pass.text.toString().trim()
        var isValid = true

        if (email.isEmpty()) {
            binding.nameContainer.error = "Username cannot be empty"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordContainer.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordContainer.error = "Password must be at least 6 characters"
            isValid = false
        }

        return isValid
    }

    private fun addTextWatcher(editText: android.widget.EditText, textInputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputLayout.error = null // Clear error once typing starts
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}
