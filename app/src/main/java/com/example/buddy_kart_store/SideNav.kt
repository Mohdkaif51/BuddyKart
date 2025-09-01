package com.example.buddy_kart_store

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.buddy_kart_store.databinding.ActivitySideNavBinding

class SideNav: AppCompatActivity() {

    private lateinit var binding: ActivitySideNavBinding

    override fun onCreate (savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySideNavBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}