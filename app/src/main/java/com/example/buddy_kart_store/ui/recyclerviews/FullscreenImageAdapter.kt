package com.example.buddy_kart_store.ui.recyclerviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.databinding.ItemRelatedImageBinding
import com.example.buddy_kart_store.model.retrofit_setup.login.RelatedImage

class FullscreenImageAdapter(private val images: List<RelatedImage> ,



    ) :
    RecyclerView.Adapter<FullscreenImageAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemRelatedImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RelatedImage) {
            Glide.with(binding.root.context)
                .load(item.image)
                .placeholder(R.drawable.download)
                .error(R.drawable.download)
                .fitCenter()
                .into(binding.relatedImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemRelatedImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size
}
