package com.example.buddy_kart_store.ui.recyclerviews

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R

class BottomBannerAdapter(private val images: List<String>) :
    RecyclerView.Adapter<BottomBannerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Glide.with(holder.itemView.context)
            .load(images[position])
            .placeholder(R.drawable.noproduct)
            .error(R.drawable.noproduct)
            .into(holder.imageView)

        Log.d("geiingIages", "onBindViewHolder: $images")


    }

    override fun getItemCount() = images.size
}
