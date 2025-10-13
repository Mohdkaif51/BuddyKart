package com.example.buddy_kart_store.model.retrofit_setup.login

import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.jvm.java

object RetrofitClient {

    val iInstance: apiService by lazy {

        val spec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
            .tlsVersions(TlsVersion.TLS_1_2)
            .allEnabledCipherSuites()
            .build()

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT))
            .protocols(listOf(okhttp3.Protocol.HTTP_1_1))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()



        Retrofit.Builder()
            .baseUrl("  https://hello.buddykartstore.com/")
            .client(okHttpClient)

            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())

            .build()
            .create(apiService::class.java)


    }


}
