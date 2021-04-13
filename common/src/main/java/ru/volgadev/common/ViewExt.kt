package ru.volgadev.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.view.View
import android.view.animation.DecelerateInterpolator
import java.lang.ref.WeakReference

private typealias Func = () -> Unit

fun View.animateScale(scaleAmplitude: Float, durationMs: Long, onEnd: Func? = null) {
    val interpolator: TimeInterpolator = DecelerateInterpolator()
    val animator = animate().apply {
        setInterpolator(interpolator)
        duration = durationMs
    }
    animator.scaleXBy(scaleAmplitude).scaleYBy(scaleAmplitude)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                onEnd?.invoke()
            }
        })
}

fun View.animateScaledVibration(scaleAmplitude: Float, durationMs: Long, onEnd: Func? = null) {
    val viewRef = WeakReference(this)
    animateScale(scaleAmplitude, durationMs / 2) {
        viewRef.get()?.animateScale(-scaleAmplitude, durationMs / 2, onEnd)
    }
}