package com.example.buddy_kart_store.ui.recyclerviews

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.buddy_kart_store.R
import androidx.core.view.isEmpty

class ImageSliderIndicator(
    private val context: Context,
    private val indicatorLayout: LinearLayout?,
    private val totalItems: Int
) {
    private val activeColor: Int
    private val inactiveColor: Int
    private var currentPosition: Int = 0

    init {
        activeColor = ContextCompat.getColor(context, R.color.active_indicator)
        inactiveColor = ContextCompat.getColor(context, R.color.inactive_indicator)
        setupIndicators()
    }

    private fun createDot(isActive: Boolean): View {
        val dot = View(context)

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 50f
            setColor(if (isActive) activeColor else inactiveColor)
        }
        dot.background = drawable

        val width = if (isActive) 40 else 16
        val height = 16

        dot.layoutParams = LinearLayout.LayoutParams(width, height).apply {
            setMargins(8, 0, 8, 0)
        }

        return dot
    }

    private fun setupIndicators() {
        indicatorLayout?.removeAllViews()
        for (i in 0 until totalItems) {
            indicatorLayout?.addView(createDot(i == 0))
        }
    }

    fun updateIndicator(position: Int?) {
        val pos = position ?: return  // skip if null
        if (pos == currentPosition) return
        val layout = indicatorLayout ?: return   // exit if null
        if (layout.isEmpty() || pos >= layout.childCount) return

        val oldDot = indicatorLayout.getChildAt(currentPosition)
        val newDot = indicatorLayout.getChildAt(pos)

        oldDot?.let { animateDot(it, false) }
        newDot?.let { animateDot(it, true) }

        currentPosition = pos
    }


    private fun animateDot(dot: View, activate: Boolean) {
        val startWidth = dot.width
        val endWidth = if (activate) 40 else 16

        val drawable = dot.background as GradientDrawable
        val endColor = if (activate) activeColor else inactiveColor

        // Animate width
        val animator = ValueAnimator.ofInt(startWidth, endWidth)
        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            dot.layoutParams.width = value
            dot.requestLayout()
        }

        drawable.setColor(endColor)

        animator.duration = 250
        animator.start()
    }
}
