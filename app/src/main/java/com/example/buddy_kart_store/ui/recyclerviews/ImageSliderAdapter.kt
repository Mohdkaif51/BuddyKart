package com.example.buddy_kart_store.ui.recyclerviews

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.utlis.BannerItem

class ImageSliderAdapter(
    private var images: MutableList<BannerItem>,
    private val viewPager: ViewGroup // pass ViewPager2 or its parent layout
) : RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]

        Glide.with(holder.itemView.context)
            .load(image.image)
//            .placeholder(R.drawable.noproduct)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    resource.let {
                        val imageWidth = it.intrinsicWidth
                        val imageHeight = it.intrinsicHeight
                        val screenWidth = holder.itemView.context.resources.displayMetrics.widthPixels
                        val newHeight = (screenWidth * imageHeight / imageWidth.toFloat()).toInt()

                        holder.imageView.post {
                            val params = viewPager.layoutParams
                            if (params.height != newHeight) {
                                params.height = newHeight
                                viewPager.layoutParams = params
                            }
                        }
                    }
                    return false
                }
            })
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = images.size

    fun updateImages(newImages: MutableList<BannerItem>) {
        images = newImages
        notifyDataSetChanged()
    }
}
