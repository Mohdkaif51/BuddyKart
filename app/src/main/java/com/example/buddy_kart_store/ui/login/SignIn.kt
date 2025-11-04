package com.example.buddy_kart_store.ui.login

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.webkit.CookieManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.databinding.ActivitySignInBinding
import com.example.buddy_kart_store.model.retrofit_setup.login.RetrofitClient
import com.example.buddy_kart_store.ui.Home.MainActivity
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern
import androidx.core.content.edit

class SignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val GOOGLE_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressbar.visibility = View.GONE

        binding.txt.setOnClickListener {
            startActivity(Intent(this, PasswordReset::class.java))
        }

        auth = FirebaseAuth.getInstance()

        setupUI()
        setupClickListeners()
        setupGoogleSignIn()
    }

    private fun setupUI() {
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        binding.signinbtn.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)

        addTextWatcher(binding.name, binding.nameContainer)
        addTextWatcher(binding.pass, binding.passwordContainer)
    }

    private fun setupClickListeners() {
        binding.signinbtn.setOnClickListener {
            binding.progressbar.visibility = View.VISIBLE
            it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click))

            if (validateInputs()) {
                val email = binding.name.text.toString()
                val password = binding.pass.text.toString()

                // ✅ Firebase Authentication first
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            loginUser(email, password)

                        } else {
                            binding.progressbar.visibility = View.GONE
                            Toast.makeText(
                                this,
                                "Password mismatch",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                binding.progressbar.visibility = View.GONE
            }
        }

        binding.signin.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }



        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // from google-services.json
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun validateInputs(): Boolean {
        val email = binding.name.text.toString().trim()
        val password = binding.pass.text.toString().trim()
        var isValid = true

        if (email.isEmpty()) {
            binding.nameContainer.error = "Email cannot be empty"
            isValid = false
        } else if (!isValidEmail(email)) {
            binding.nameContainer.error = "Please enter a valid email address"
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

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
        return emailPattern.matcher(email).matches()
    }

    private fun addTextWatcher(editText: EditText, textInputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputLayout.error = null
                textInputLayout.isErrorEnabled = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
        }
    }

    private fun loginUser(email: String, password: String) {
        RetrofitClient.iInstance.loginUser(
            route = "wbapi/wblogin.getlogin",
            email = email,
            password = password
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                binding.progressbar.visibility = View.GONE

                val responseBody = response.body()?.string()
                Log.d("LoginResponse", "Response: $responseBody")

                // Avoid parsing HTML responses
                if (!response.isSuccessful || responseBody.isNullOrEmpty() || responseBody.trimStart()
                        .startsWith("<!DOCTYPE")
                ) {
                    Log.e("LoginError", "Invalid response: $responseBody")
                    Toast.makeText(
                        this@SignIn,
                        "Server returned invalid response",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                try {
                    var rawResponse = responseBody ?: ""

                    // 🧹 Clean out PHP warnings or HTML before JSON
                    if (rawResponse.contains("{")) {
                        rawResponse = rawResponse.substring(rawResponse.indexOf("{"))
                    }

                    // If still invalid or HTML-only, stop
                    if (rawResponse.trimStart().startsWith("<") || rawResponse.isEmpty()) {
                        Log.e("LoginError", "Invalid response (HTML or empty): $rawResponse")
                        Toast.makeText(
                            this@SignIn,
                            "Server returned invalid response",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val jsonObject = JSONObject(rawResponse)
                    val success = jsonObject.optString("success") == "true"
                    val message = jsonObject.optString("message", "Something went wrong")
                    val sessionId = jsonObject.optString("session_id", "")

//                    --------------------------------------------
                    val cookieManager = CookieManager.getInstance()
                    val prefs = getSharedPreferences("opencart_prefs", MODE_PRIVATE)
                    val savedCookies = prefs.getString("OC_Cookies", null)
                    Log.d("gettinnggcookiee", "onResponse: $savedCookies")

                    if (!savedCookies.isNullOrEmpty()) {
                        // ✅ Set cookies for your domain
                        cookieManager.setCookie("https://hellobuddy.jkopticals.com/", savedCookies)
                        cookieManager.flush()
                        Log.d("COOKIE", "Restored cookies: $savedCookies")

                        // ✅ Extract only OCSESSID value
                        val sessionId = savedCookies
                            .substringAfter("OCSESSID=")   // get everything after 'OCSESSID='
                            .substringBefore(";")          // stop before next semicolon
                            .trim()

                        SessionManager.saveSessionId(this@SignIn, sessionId)
                    } else {
                        Log.d("COOKIE", "No cookies found — fresh session will start")
                    }



//                    -----------------------------------------



                    Log.d("sessionidddddddd", "onCreate: $sessionId")

                    val data = jsonObject.optJSONObject("data")
                    var token: String? = null

                    if (data != null) {
                        token = data.optString("token", null)
                        val customerId = data.optString("customer_id")
                        val firstName = data.optString("firstname")
                        val lastName = data.optString("lastname")
                        val emailResp = data.optString("email")
                        val mobileNumber = data.optString("telephone")

                        Log.d("LoginData", "CustomerID: $customerId, Name: $firstName $lastName, Email: $emailResp")

                        val sharedPref = Sharedpref(applicationContext)
                        sharedPref.saveUser(
                            firstName,
                            lastName,
                            mobileNumber,
                            email,
                            password,
                            customerId
                        )
                        val allPrefs = sharedPref.prefs.all
                        Log.d("SharedPreflogin", "All prefs after save: $allPrefs")

                        SessionManager.saveToken(this@SignIn, token ?: sessionId, sessionId)
                        SessionManager.saveCustomerId(this@SignIn, customerId)
//                        SessionManager.saveSessionId(this@SignIn, sessionId)
                    }

                    if (success) {
                        saveTokenSecure(token ?: sessionId)
                        Sharedpref(this@SignIn).setLogin(true)
                        Toast.makeText(this@SignIn, "Login Success", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SignIn, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@SignIn, message, Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Log.e("LoginError", "Parsing error", e)
                    Toast.makeText(this@SignIn, "Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                binding.progressbar.visibility = View.GONE
                Toast.makeText(this@SignIn, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("LoginError", "Network error", t)
            }
        })
    }

    private fun saveTokenSecure(token: String) {
        val masterKey = MasterKey.Builder(this@SignIn)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            this@SignIn,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        sharedPreferences.edit { putString("auth_token", token) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            Log.d("googleLogin", "onActivityResult: $task")
            try {
                // ✅ Get Google account
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                // ✅ Firebase auth
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        binding.progressbar.visibility = View.GONE

                        if (authTask.isSuccessful) {
                            val user = auth.currentUser
                            val email = user?.email ?: ""
                            val name = user?.displayName ?: ""
                            val uid = user?.uid ?: ""

                            loginUser(email, "")

                            Toast.makeText(
                                this,
                                "Welcome Back: $name",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
//                            Toast.makeText(
//                                this,
//                                "Firebase Sign-In Failed: ${authTask.exception?.message}",
//                                Toast.LENGTH_SHORT
//                            ).show()
                        }
                    }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Error code: ${e.statusCode}, message: ${e.message}")
                Toast.makeText(this, "Google Sign-In Failed: ${e.statusCode}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
