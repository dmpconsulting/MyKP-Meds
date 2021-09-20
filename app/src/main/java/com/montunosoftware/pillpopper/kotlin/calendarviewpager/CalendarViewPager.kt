package com.montunosoftware.pillpopper.kotlin.calendarviewpager

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager

class HistoryCalendarViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMeasure: Int
        var height = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val h = child.measuredHeight
            if (h > height) height = h
        }
        heightMeasure = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightMeasure)
    }

    override fun setLayoutDirection(layoutDirection: Int) {
        if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) {
            rotationY = 0f
        } else if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
            rotationY = 180f
        }
    }
}