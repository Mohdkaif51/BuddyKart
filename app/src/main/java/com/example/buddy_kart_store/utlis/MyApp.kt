package com.example.buddy_kart_store.utlis

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import com.google.firebase.FirebaseApp

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)
        settings = Settings()

    }

    companion object {
        lateinit var instance: MyApp
            private set
        lateinit var settings: Settings

    }
    class Settings {
        var sessionId: String? = null
        // other fields...
    }



    object AppPrefs {

        private const val PREFS_NAME = "app_prefs"
        private const val KEY_HOME_API_CALLED = "home_api_called"

        fun isHomeApiCalled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_HOME_API_CALLED, false)
        }

        fun setHomeApiCalled(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_HOME_API_CALLED, true).apply()
        }
    }

}
