package com.example.buddy_kart_store.ui.recyclerviews

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.databinding.ItemRelatedImageBinding
import com.example.buddy_kart_store.model.retrofit_setup.login.RelatedImage
import kotlin.jvm.java

class RelatedImagesPagerAdapter(
    private var images: List<RelatedImage>,
    private val onImageClick: (Int) -> Unit,


) : RecyclerView.Adapter<RelatedImagesPagerAdapter.ImageViewHolder>() {

    fun updateList(newImages: List<RelatedImage>) {
        images = newImages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemRelatedImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        holder.bind(image)

        holder.itemView.setOnClickListener {
            onImageClick(position)
        }

    }

    override fun getItemCount() = images.size

    class ImageViewHolder(private val binding: ItemRelatedImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: RelatedImage) {
            Glide.with(binding.root.context)
                .load(image.image)
                .placeholder(R.drawable.download)
                .error(R.drawable.download)
                .into(binding.relatedImage)
        }
    }
}
