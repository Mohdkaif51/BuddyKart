package com.example.buddy_kart_store.utils

import android.content.Context
import android.util.Log

class Sharedpref(context: Context) {
    val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)


    //this is for login
    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_WISHLIST_IDS = "wishlist_ids" // NEW

    }

    fun setLogin(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    //    this  for registration
    fun saveUser(
        firstname: String,
        lastname: String,
        phonenumber: String,
        email: String,
        password: String,
        customerId: String
    ) {
        val editor = prefs.edit()
        editor.putString("FIRST_NAME", firstname)
        editor.putString("LAST_NAME", lastname)
        editor.putString("PHONE", phonenumber)
        editor.putString("EMAIL", email)
        editor.putString("PASSWORD", password)
        editor.putString("CUSTOMER_ID", customerId)
        editor.putBoolean("IS_LOGGED_IN", true)
        val success = editor.commit() // returns true if saved successfully
        Log.d("SharedPref", "Save success: $success, CustomerId: $customerId")
    }


    fun getEmail(): String? = prefs.getString("EMAIL", null)
    fun getPassword(): String? = prefs.getString("PASSWORD", null)
    fun isRegisterd(): Boolean = prefs.getBoolean("IS_LOGGED_IN", false)
    fun getCustomerId(): String? = prefs.getString("CUSTOMER_ID", null)

    fun getName(): String? =
        prefs.getString("FIRST_NAME", null) + " " + prefs.getString("LAST_NAME", null)


    //this is for logout
    fun logout() {
        prefs.edit().clear().apply()
    }


//    for wishlist

    fun addToWishlist(productId: Int) {
        val ids = prefs.getStringSet(KEY_WISHLIST_IDS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        ids.add(productId.toString())
        prefs.edit().putStringSet(KEY_WISHLIST_IDS, ids).apply()
    }

    // Remove product ID from wishlist
    fun removeFromWishlist(productId: Int) {
        val ids = prefs.getStringSet(KEY_WISHLIST_IDS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        ids.remove(productId.toString())
        prefs.edit().putStringSet(KEY_WISHLIST_IDS, ids).apply()
    }

    // Check if product is in wishlist
    fun isInWishlist(productId: Int): Boolean {
        val ids = prefs.getStringSet(KEY_WISHLIST_IDS, mutableSetOf()) ?: mutableSetOf()
        return ids.contains(productId.toString())
    }

    // Get all wishlist IDs
    fun getWishlist(): Set<String> = prefs.getStringSet(KEY_WISHLIST_IDS, mutableSetOf()) ?: mutableSetOf()

    // Clear wishlist (optional)
    fun clearWishlist() {
        prefs.edit().remove(KEY_WISHLIST_IDS).apply()
    }
}
