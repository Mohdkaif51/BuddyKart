package com.example.buddy_kart_store.utlis

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import androidx.core.content.edit

class CookieInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        val cookies = response.headers("Set-Cookie")
        if (cookies.isNotEmpty()) {
            val joinedCookies = cookies.joinToString("; ")
            Log.d("COOKIE_SAVE", "Received cookies: $joinedCookies")

            val prefs = context.getSharedPreferences("opencart_prefs", Context.MODE_PRIVATE)
            prefs.edit { putString("OC_Cookies", joinedCookies) }
        }

        return response
    }
}
