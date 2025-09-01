package com.example.buddy_kart_store

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.buddy_kart_store.databinding.ActivitySignUpBinding
import com.google.android.material.textfield.TextInputLayout

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signup.setOnClickListener {
           val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }

        // Handle signup button click
        binding.signinbtn.setOnClickListener {
            validateFields()
        }

        // Clear error when typing again
        clearErrorOnTyping(binding.signupEmail, binding.signupNameContainer)
        clearErrorOnTyping(binding.pass, binding.signinPasswordContainer)
        clearErrorOnTyping(binding.confpass, binding.confirmpasswordContainer)
    }

    private fun validateFields() {
        val email = binding.signupEmail.text.toString().trim()
        val password = binding.pass.text.toString().trim()
        val confirmPassword = binding.confpass.text.toString().trim()

        var isValid = true

        // Email validation
        if (email.isEmpty()) {
            setError(binding.signupNameContainer, "Enter Email")
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError(binding.signupNameContainer, "Enter valid Email")
            isValid = false
        }

        // Password validation
        if (password.isEmpty()) {
            setError(binding.signinPasswordContainer, "Enter Password")
            isValid = false
        } else if (password.length < 6) {
            setError(binding.signinPasswordContainer, "Password must be at least 6 characters")
            isValid = false
        }

        // Confirm password validation
        if (confirmPassword.isEmpty()) {
            setError(binding.confirmpasswordContainer, "Confirm your Password")
            isValid = false
        } else if (password != confirmPassword) {
            setError(binding.confirmpasswordContainer, "Passwords do not match")
            isValid = false
        }

        if (isValid) {
            // ✅ All validations passed
            // TODO: Continue with signup logic (Firebase/Auth/Database)
        }
    }

    private fun setError(textInputLayout: TextInputLayout, message: String) {
        textInputLayout.isErrorEnabled = true
        textInputLayout.error = message
    }

    private fun clearErrorOnTyping(
        editText: com.google.android.material.textfield.TextInputEditText,
        textInputLayout: TextInputLayout
    ) {
        editText.addTextChangedListener {
            textInputLayout.isErrorEnabled = false
            textInputLayout.error = null
        }
    }
}
