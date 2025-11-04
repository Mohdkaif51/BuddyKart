package com.example.buddy_kart_store.utils

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager2.widget.ViewPager2
import android.view.View
import android.widget.FrameLayout

class WrapContentViewPager2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var viewPager: ViewPager2? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        // find the first ViewPager2 child
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is ViewPager2) {
                viewPager = child
                break
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val pager = viewPager ?: return
        var maxHeight = 0
        for (i in 0 until pager.childCount) {
            val child: View = pager.getChildAt(i)
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val h = child.measuredHeight
            if (h > maxHeight) maxHeight = h
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), maxHeight)
    }
}