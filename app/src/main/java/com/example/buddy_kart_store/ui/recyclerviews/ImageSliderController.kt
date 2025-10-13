package com.example.buddy_kart_store.ui.recyclerviews

import android.os.Handler
import android.os.Looper
import androidx.viewpager2.widget.ViewPager2

class ImageSliderController(
    private val viewPager: ViewPager2,
    private var autoScrollInterval: Long = 4000L
) {

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var isRunning = false

    init {
        // Pause auto-scroll when user drags
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    ViewPager2.SCROLL_STATE_DRAGGING -> stopAutoScrollInternal()
                    ViewPager2.SCROLL_STATE_IDLE -> if (isRunning) startAutoScrollInternal()
                }
            }
        })
    }

    fun startAutoScroll() {
        if (isRunning) return
        isRunning = true
        startAutoScrollInternal()
    }

    private fun startAutoScrollInternal() {
        stopAutoScrollInternal() // remove any pending callbacks
        runnable = object : Runnable {
            override fun run() {
                val adapter = viewPager.adapter ?: return
                val count = adapter.itemCount
                if (count <= 1) return

                val next = (viewPager.currentItem + 1) % count
                viewPager.setCurrentItem(next, true)
                handler.postDelayed(this, autoScrollInterval)
            }
        }
        handler.postDelayed(runnable!!, autoScrollInterval)
    }

    fun stopAutoScroll() {
        isRunning = false
        stopAutoScrollInternal()
    }

    private fun stopAutoScrollInternal() {
        runnable?.let { handler.removeCallbacks(it) }
    }

    fun setAutoScrollInterval(interval: Long) {
        autoScrollInterval = interval
        if (isRunning) {
            stopAutoScrollInternal()
            startAutoScrollInternal()
        }
    }
}
