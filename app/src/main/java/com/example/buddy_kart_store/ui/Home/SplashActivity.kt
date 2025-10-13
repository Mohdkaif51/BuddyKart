package com.example.buddy_kart_store.ui.Home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.buddy_kart_store.ui.Home.MainActivity
import com.example.buddy_kart_store.ui.login.SignIn
import com.example.buddy_kart_store.utils.Sharedpref
import kotlin.jvm.java

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPref: Sharedpref

    override fun onCreate(savedInstanceState: Bundle?) {
        // Show splash
        installSplashScreen()
        super.onCreate(savedInstanceState)


        sharedPref = Sharedpref(this)

        if (sharedPref.isLoggedIn()) {
            // Already logged in → go to Main
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Not logged in → go to SignIn
            startActivity(Intent(this, SignIn::class.java))
        }

        finish() // Prevent back to splash
    }
}