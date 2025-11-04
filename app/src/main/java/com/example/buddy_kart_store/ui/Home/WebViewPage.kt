package com.example.buddy_kart_store.ui.Home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.databinding.ActivityWebViewPageBinding
import com.example.buddy_kart_store.utils.Sharedpref
import com.example.buddy_kart_store.utlis.SessionManager
import java.util.Random
import kotlin.jvm.java
import androidx.core.graphics.drawable.toDrawable

class WebViewPage : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewPageBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webView = binding.checkoutWebView
        val progressBar = binding.progressBar

        // ✅ Setup CookieManager properly
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        // ❌ DO NOT remove all cookies every time (that deletes your session)
        // cookieManager.removeAllCookies(null)

        // ✅ Restore cookies from SharedPreferences
        val prefs = getSharedPreferences("opencart_prefs", MODE_PRIVATE)
        val savedCookies = prefs.getString("OC_Cookies", null)
        val baseUrl = "https://hellobuddy.jkopticals.com/"

        if (!savedCookies.isNullOrEmpty()) {
            for (cookie in savedCookies.split(";")) {
                val trimmed = cookie.trim()
                if (trimmed.isNotEmpty()) {
                    cookieManager.setCookie(baseUrl, "$trimmed; Path=/; Domain=hellobuddy.jkopticals.com")
                }
            }
            cookieManager.flush()
            Log.d("COOKIE", "✅ Applied saved cookies: $savedCookies")
        } else {
            Log.d("COOKIE", "⚠️ No saved cookies found — guest checkout.")
        }

        // ✅ Extract OCSESSID (if any)
        val sessionId: String? = savedCookies
            ?.split(";")
            ?.map { it.trim() }
            ?.firstOrNull { it.startsWith("OCSESSID=") }
            ?.substringAfter("OCSESSID=")

        Log.d("SESSION", "OCSESSID = $sessionId")

        // ✅ Retrieve user/customer info
        val customerId = SessionManager.getCustomerId(this)
        Log.d("SESSION", "Customer ID: $customerId")
        val coupon = ""

        // ✅ Construct checkout URL
        val checkoutUrl =
           "https://hellobuddy.jkopticals.com/index.php?route=checkout/checkout.indexcheckout&language=en-gb&customer_id=$customerId&guest_id=$sessionId&coupon=$coupon&random=${getRandomString(8)}"


//        val url="https://wishingbasket.com/index.php?route=checkout/checkout.indexcheckout&language=en-gb&customer_id=$customerId&guest_id=$guestId&coupon=$coupon&random=${getRandomString(8)}"


        Log.d("CheckoutURL", "Final URL -> $checkoutUrl")

        // ✅ Configure WebView settings
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        // ✅ Add JavaScript bridge
        webView.addJavascriptInterface(JSBridge(), "Android")

        // ✅ Handle navigation + cookies
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                Log.d("WebView", "Redirecting to: $url")
                return false // Let WebView handle it
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
                Log.d("WebView", "Page started: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                Log.d("WebView", "✅ Page finished loading: $url")

                // ✅ Update and save cookies safely
                val cookiesNow = cookieManager.getCookie(baseUrl)
                if (!cookiesNow.isNullOrEmpty()) {
                    prefs.edit { putString("OC_Cookies", cookiesNow) }
                    Log.d("Cookies", "✅ Cookies updated successfully.")
                }

                // ✅ Detect checkout success/failure
                when {
                    url?.contains("checkout/success", ignoreCase = true) == true -> {
                        Log.d("Checkout", "✅ Checkout success detected")
                        showOrderSuccessPopup()


                        // 🧹 Clear the entire cart once order completes
                        Sharedpref.CartPref.clearCart(this@WebViewPage)
                        Log.d("CartPrefs", "🧹 Cart cleared after successful checkout")

                        // 🎉 Notify user
                        Toast.makeText(this@WebViewPage, "Order placed successfully!", Toast.LENGTH_SHORT).show()

                        // ✅ Optional: navigate to a success screen instead of finishing directly
                        // startActivity(Intent(this@WebViewPage, OrderSuccessActivity::class.java))
                        finish()
                    }

                    url?.contains("checkout/failure", ignoreCase = true) == true -> {
                        Log.d("Checkout", "❌ Checkout failed")
                        Toast.makeText(this@WebViewPage, "Order failed. Please try again.", Toast.LENGTH_SHORT).show()

                        // 🛒 Redirect user back to cart
                        startActivity(Intent(this@WebViewPage, MainActivity::class.java))
                        finish()
                    }

                    else -> {
                        Log.d("Checkout", "ℹ️ Regular page loaded (not success/failure)")
                    }
                }

                // ✅ Session check
                when {
                    cookiesNow?.contains("OCSESSID") == true ->
                        Log.d("SessionCheck", "✅ OCSESSID active — session preserved.")
                    else ->
                        Log.d("SessionCheck", "⚠️ OCSESSID missing — guest checkout likely.")
                }
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }
        }

        // ✅ ProgressBar updates
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }
        }

        // ✅ Load the checkout page (after cookies restored)
        webView.loadUrl(checkoutUrl)

        // ✅ Back button behavior
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    // ✅ JS interface for site-to-app callbacks
    inner class JSBridge {
        @JavascriptInterface
        fun failureCheckout() {
            startActivity(Intent(this@WebViewPage, CartActivity::class.java))
            finish()
        }
    }

    // ✅ Utility for random string
    private fun getRandomString(length: Int): String {
        val allowed = "0123456789abcdefghijklmnopqrstuvwxyz"
        return (1..length)
            .map { allowed.random() }
            .joinToString("")
    }
    private fun showOrderSuccessPopup() {
        val dialogView = layoutInflater.inflate(R.layout.orderconfirmation, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.show()

        // 🕒 Automatically dismiss after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            Sharedpref.CartPref.clearCart(this)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }

}
