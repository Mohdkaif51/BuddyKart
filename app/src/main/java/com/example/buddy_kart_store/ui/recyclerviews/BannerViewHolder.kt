package com.example.buddy_kart_store.ui.recyclerviews

import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.utlis.BannerItem
import com.example.buddy_kart_store.utlis.HomeModule
import kotlin.collections.toMutableList

class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPager)
    private val indicatorLayout: LinearLayout = itemView.findViewById(R.id.dotIndicatorLayout)
    private val imageSlider = ImageSlider(itemView.context)
    private var adapter: ImageSliderAdapter? = null

    fun bind(banners: List<BannerItem>) {
        if (banners.isEmpty()) return

        imageSlider.initialize(viewPager, indicatorLayout)

        // Sanitize URLs before updating the adapter
        val sanitizedBanners = banners.map { banner ->
            banner.copy(image = cleanUrl(banner.image))
        }

        if (adapter == null) {
            adapter = ImageSliderAdapter(sanitizedBanners.toMutableList())
            viewPager.adapter = adapter
        } else {
            adapter?.updateImages(sanitizedBanners.toMutableList())
        }

        imageSlider.setupSlider(sanitizedBanners.toMutableList())
    }

    // Helper function to fix duplicated URLs
    private fun cleanUrl(url: String): String {
        return url.replace(
            "https://hello.buddykartstore.com/image/https://hello.buddykartstore.com/image/",
            "https://hello.buddykartstore.com/image/"
        )
    }
}
