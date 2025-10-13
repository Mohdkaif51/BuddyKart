package com.example.buddy_kart_store.ui.recyclerviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.databinding.ItemRelatedImageBinding
import com.example.buddy_kart_store.model.retrofit_setup.login.RelatedImage

class RelatedImagesPagerAdapter(
    private var images: List<RelatedImage>
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
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size

    class ImageViewHolder(private val binding: ItemRelatedImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(image: RelatedImage) {
            Glide.with(binding.root.context)
                .load(image.image)
                .placeholder(R.drawable.download)
                .error(R.drawable.download)
                .into(binding.relatedImage)
        }
    }
}
