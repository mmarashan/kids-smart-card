package ru.volgadev.common.view.recyclerview

import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class CenterSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int = (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
}

fun RecyclerView.scrollToItemToCenter(position: Int) {
    val scroller = CenterSmoothScroller(this.context).apply {
        targetPosition = position
    }
    layoutManager?.startSmoothScroll(scroller)
}