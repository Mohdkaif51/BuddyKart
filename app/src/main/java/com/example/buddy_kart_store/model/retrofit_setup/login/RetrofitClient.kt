package com.example.buddy_kart_store.model.retrofit_setup.login

import android.content.Context
import android.util.Log
import com.example.buddy_kart_store.utlis.MyApp
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    val iInstance: apiService by lazy {

        val spec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
            .tlsVersions(TlsVersion.TLS_1_2)
            .allEnabledCipherSuites()
            .build()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // ✅ FIX: use Application context from MyApp singleton
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(CookieInterceptor(MyApp.instance.applicationContext))
            .connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT))
            .protocols(listOf(Protocol.HTTP_1_1))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://hellobuddy.jkopticals.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(apiService::class.java)
    }

    // ✅ Cookie Interceptor for saving cookies to SharedPreferences
    class CookieInterceptor(private val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())

            val cookies = response.headers("Set-Cookie")
            if (cookies.isNotEmpty()) {
                val joinedCookies = cookies.joinToString("; ")
                Log.d("COOKIE_SAVE", "Received cookies: $joinedCookies")

                val prefs = context.getSharedPreferences("opencart_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("OC_Cookies", joinedCookies).apply()
            }

            return response
        }
    }
}
