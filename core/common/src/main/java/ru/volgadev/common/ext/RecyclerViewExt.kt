package ru.volgadev.common.ext

import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.scrollToItemInCenter(position: Int) {
    val scroller = CenterSmoothScroller(this.context).apply {
        targetPosition = position
    }
    layoutManager?.startSmoothScroll(scroller)
}

private class CenterSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int = (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
}
