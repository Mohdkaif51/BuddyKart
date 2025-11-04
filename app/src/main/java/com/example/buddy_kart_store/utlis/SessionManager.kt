package com.example.buddy_kart_store.utlis

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.buddy_kart_store.ui.login.SignIn

object SessionManager {

    private const val PREF_NAME = "secure_prefs"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_CUSTOMER_ID = "customer_id"

    private const val KEY_SESSION_ID = "session_id"
    private const val PREFS_NAME = "UserSession"
    private const val KEY_GUEST_ID = "guest_id"

    private fun getSharedPreferences(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveToken(context: SignIn, token: String?, sessionId: String) {
        val sharedPreferences = getSharedPreferences(context as Context)
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearToken(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        sharedPreferences.edit().clear().apply()
    }

    fun saveCustomerId(context: SignIn, customerId: String) {
        val sharedPreferences = getSharedPreferences(context as Context)
        sharedPreferences.edit().putString(KEY_CUSTOMER_ID, customerId).apply()
    }

    fun getCustomerId(context: Context): String? {
        val sharedPreferences = getSharedPreferences(context)
        return sharedPreferences.getString(KEY_CUSTOMER_ID, null)

    }

    fun clearCustomerId(context: Context) {
        val sharedPreferences = getSharedPreferences(context)
        sharedPreferences.edit().clear().apply()
    }



    fun saveSessionId(context: Context, sessionId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SESSION_ID, sessionId).apply()
    }

    fun getSessionId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SESSION_ID, null)
    }

    fun saveGuestId(context: Context, guestId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_GUEST_ID, guestId)
            .apply()
    }

    fun getGuestId(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GUEST_ID, null)
    }

}
