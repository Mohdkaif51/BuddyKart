package com.example.buddy_kart_store

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.buddy_kart_store.databinding.ActivityMainBinding
import kotlin.jvm.java

class MainActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate (savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sidenavigation.setOnClickListener {
            val intent = Intent(this, SideNav::class.java)
            startActivity(intent)
        }
        binding.button.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }




    }
}