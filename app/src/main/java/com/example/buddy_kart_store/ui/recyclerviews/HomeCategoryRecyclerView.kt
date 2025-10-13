package com.example.buddy_kart_store.ui.recyclerviews

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.model.retrofit_setup.login.TopCategory
import com.example.buddy_kart_store.ui.Home.Categorypage

class TopCategoryAdapter(private val categories: MutableList<TopCategory>) :
    RecyclerView.Adapter<TopCategoryAdapter.TopCategoryViewHolder>() {

    inner class TopCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.categoryImage)
        val textView: TextView = itemView.findViewById(R.id.categoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.homepagecategory, parent, false)
        return TopCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopCategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.textView.text = category.name

        Log.d("gettingtopcate", "onBindViewHolder: $category")

        Glide.with(holder.itemView.context)
            .load(category.image)
            .placeholder(R.drawable.noproduct)
            .error(R.drawable.noproduct)
            .into(holder.imageView)


//        holder.itemView.setOnClickListener {
//            val context = holder.itemView.context
//            val intent = Intent(context, Categorypage::class.java)
//            intent.putExtra("category_id", category.id)
//            intent.putExtra("category_name", category.name)
//            context.startActivity(intent)
//            Log.d("gettingtopcate", "onBindViewHolder: ${category.id}")
//        }

        holder.itemView.setOnClickListener {
            Categorypage.launch(holder.itemView.context, category.id.toInt())

        }
    }



    override fun getItemCount(): Int = categories.size

    // Optional: update list dynamically
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<TopCategory>) {
        categories.clear()
        categories.addAll(newList)
        notifyDataSetChanged()
    }
}
