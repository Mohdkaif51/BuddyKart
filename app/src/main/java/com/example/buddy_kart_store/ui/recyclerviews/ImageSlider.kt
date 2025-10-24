package com.example.buddy_kart_store.ui.recyclerviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.buddy_kart_store.R
import com.example.buddy_kart_store.utlis.BannerItem

class ImageSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var viewPager: ViewPager2? = null
    private var indicatorLayout: LinearLayout? = null
    private var sliderController: ImageSliderController? = null
    private var sliderIndicator: ImageSliderIndicator? = null

    private var pageChangeCallback: ViewPager2.OnPageChangeCallback? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_image_slider, this, true)
        orientation = VERTICAL
    }

    fun initialize(viewPager: ViewPager2, indicatorLayout: LinearLayout) {
        this.viewPager = viewPager
        this.indicatorLayout = indicatorLayout

        // Create controller only once
        if (sliderController == null) {
            sliderController = ImageSliderController(viewPager)
        }
    }

    fun setupSlider(images: MutableList<BannerItem>, adapter: RecyclerView.Adapter<*>? = null) {
        if (images.isEmpty()) return

        viewPager?.adapter = adapter ?: ImageSliderAdapter(images)
        viewPager?.offscreenPageLimit = 3

        // Setup indicator
        sliderIndicator = ImageSliderIndicator(context, indicatorLayout, images.size)
        sliderIndicator?.updateIndicator(0)

        // Page transformer for smooth effect
        viewPager?.setPageTransformer { page, position ->
            val absPos = kotlin.math.abs(position)
            page.apply {
                scaleX = 1 - (absPos * 0.05f)
                scaleY = 1 - (absPos * 0.05f)
                alpha = 0.8f + (1 - absPos) * 0.2f
            }
        }

        // Remove old callback
        pageChangeCallback?.let { viewPager?.unregisterOnPageChangeCallback(it) }
        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val actualPosition = position % images.size
                sliderIndicator?.updateIndicator(actualPosition)
            }
        }
        viewPager?.registerOnPageChangeCallback(pageChangeCallback!!)

        // Start auto-scroll
        sliderController?.startAutoScroll()
    }

    fun startAutoScroll() = sliderController?.startAutoScroll()
    fun stopAutoScroll() = sliderController?.stopAutoScroll()
    fun setAutoScrollInterval(interval: Long) = sliderController?.setAutoScrollInterval(interval)
    fun setCurrentItem(position: Int, smoothScroll: Boolean = true) =
        viewPager?.setCurrentItem(position, smoothScroll)

    fun cleanup() {
        viewPager?.let {
            it.adapter = null
            pageChangeCallback?.let { callback -> it.unregisterOnPageChangeCallback(callback) }
        }
        sliderController?.stopAutoScroll()
        sliderController = null
        pageChangeCallback = null
        sliderIndicator = null
    }
}
