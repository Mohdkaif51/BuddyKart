package com.example.buddy_kart_store.ui.Home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.ui.Home.MainActivity
import com.example.buddy_kart_store.ui.login.SignIn
import com.example.buddy_kart_store.utils.Sharedpref
import kotlin.jvm.java

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPref: Sharedpref

    override fun onCreate(savedInstanceState: Bundle?) {
//        installSplashScreen()  // System splash
        super.onCreate(savedInstanceState)

        sharedPref = Sharedpref(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (sharedPref.isLoggedIn()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, SignIn::class.java))
            }
            finish()
        }, 0)
    }
}
