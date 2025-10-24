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


    // Save cart product IDs
    object CartPrefs {
        private const val PREFS_NAME = "cart_prefs"
        private const val KEY_CART_IDS = "cart_ids"

        // ✅ Save all cart_ids as StringSet
        fun saveCartIds(context: Context, cartIds: Set<String>) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putStringSet(KEY_CART_IDS, cartIds).apply()
        }

        fun getCartIds(context: Context): Set<String> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            try {
                prefs.getInt(KEY_CART_IDS, -1) // just try reading as Int
                prefs.edit().remove(KEY_CART_IDS).apply()
            } catch (e: ClassCastException) {
            }

            return prefs.getStringSet(KEY_CART_IDS, emptySet()) ?: emptySet()
        }

        fun addProductId(context: Context, cartId: String) {
            val currentIds = getCartIds(context).toMutableSet()
            currentIds.add(cartId)
            saveCartIds(context, currentIds)
        }

        fun removeProductId(context: Context, cartId: String) {
            val currentIds = getCartIds(context).toMutableSet()
            currentIds.remove(cartId)
            saveCartIds(context, currentIds)
        }

        fun clearCart(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
    }

    fun saveCartId(cartId: String) {
        val existingSet =
            prefs.getStringSet("CART_IDS", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        existingSet.add(cartId)

        prefs.edit().apply {
            putStringSet("CART_IDS", existingSet)
            apply()
        }


    }

    fun getCartId(): Set<String> {
        return prefs.getStringSet("CART_IDS", emptySet()) ?: emptySet()
    }


    object WishlistPrefs {

        private const val PREFS_NAME = "wishlist_prefs"
        private const val KEY_WISHLIST_IDS = "wishlist_ids"

        // Save wishlist product IDs
        fun saveWishlistIds(context: Context?, productIds: Set<String>) {
            val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs?.edit()?.putStringSet(KEY_WISHLIST_IDS, productIds)?.apply()
        }

        // Get saved wishlist product IDs
        fun getWishlistIds(context: Context): Set<String> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getStringSet(KEY_WISHLIST_IDS, emptySet()) ?: emptySet()
        }

        // Add a single product ID to wishlist
        fun addProductId(context: Context, productId: String) {
            val ids = getWishlistIds(context).toMutableSet()
            ids.add(productId)
            saveWishlistIds(context, ids)
        }

        // Remove a single product ID from wishlist
        fun removeProductId(context: Context, productId: String) {
            val ids = getWishlistIds(context).toMutableSet()
            ids.remove(productId)
            saveWishlistIds(context, ids)
        }

        // Clear all wishlist items
        fun clearWishlist(context: Context) {
            saveWishlistIds(context, emptySet())
        }

        // Optional: Check if a product is already in wishlist
        fun isInWishlist(context: Context, productId: String): Boolean {
            return getWishlistIds(context).contains(productId)
        }
    }


    object CartPref {
        private const val PREFS_NAME = "cart_pref"
        private const val KEY_CART_QUANTITIES = "cart_quantities"
        private const val KEY_CART_MAPPING = "cart_mapping" // productId -> cartId


        // ✅ Save mapping: productId -> cartId
        fun saveCartMapping(context: Context, productId: String, cartId: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("cartId_for_$productId", cartId).apply()
        }

        // ✅ Get the specific cartId for a product
        fun getCartIdForProduct(context: Context, productId: String): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString("cartId_for_$productId", null)
        }

        fun isInCart(context: Context, productId: String): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val cartId = prefs.getString("cartId_for_$productId", null)
            return cartId != null
        }


        // ✅ Optional: clear all cart mappings
        fun clearCart(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val allKeys = prefs.all.keys
            val editor = prefs.edit()
            for (key in allKeys) {
                if (key.startsWith("qty_") || key.startsWith("cartId_for_")) {
                    editor.remove(key)
                }
            }
            editor.apply()
        }


        fun saveCartQuantities(context: Context, quantities: Map<String, Int>) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            for ((productId, qty) in quantities) {
                editor.putInt("qty_$productId", qty)
            }
            editor.apply()
        }

        // Get all quantities from SharedPreferences
        fun getCartQuantities(context: Context): Map<String, Int> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val allEntries = prefs.all
            val quantities = mutableMapOf<String, Int>()
            for ((key, value) in allEntries) {
                if (key.startsWith("qty_")) {
                    val productId = key.removePrefix("qty_")
                    quantities[productId] = value as? Int ?: 0
                }
            }
            return quantities
        }

        //
        fun deleteProduct(context: Context, productId: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()

            // Remove quantity
            editor.remove("qty_$productId")
            // Remove cartId mapping
            editor.remove("cartId_for_$productId")

            editor.apply()
        }


    }




}
