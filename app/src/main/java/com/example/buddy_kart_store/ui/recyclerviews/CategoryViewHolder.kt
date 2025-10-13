package com.example.buddy_kart_store.ui.recyclerviews

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.TopCategory
import com.example.buddy_kart_store.ui.Home.Categorypage

class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(categories: MutableList<TopCategory>) {
        val recyclerView = itemView.findViewById<RecyclerView>(R.id.categoryRecyclerView)
        recyclerView.layoutManager =
            LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = TopCategoryAdapter(categories)

        val button = itemView.findViewById<TextView>(R.id.Viewall)
        button.setOnClickListener {
            val Intent = Intent(itemView.context, Categorypage::class.java)
            itemView.context.startActivity(Intent)
        }
    }
}
