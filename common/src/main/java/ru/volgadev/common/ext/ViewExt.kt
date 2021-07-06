package ru.volgadev.common.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.transition.Transition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import java.lang.ref.WeakReference
import kotlin.math.min

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

fun View.setVisibleWithTransition(
    visibility: Int,
    transition: Transition,
    durationMs: Long,
    parent: ViewGroup,
    delayMs: Long = 0L
) {
    transition.duration = durationMs
    transition.startDelay = delayMs
    transition.addTarget(this)
    TransitionManager.beginDelayedTransition(parent, transition)
    this.visibility = visibility
}

fun View.runSwingAnimation(amplitudeZ: Float, period: Long) {
    val viewRef = WeakReference<View>(this)
    val interpolator: TimeInterpolator = DecelerateInterpolator()
    val animator = this.animate().apply {
        setInterpolator(interpolator)
        duration = period / 2
    }
    animator.rotationBy(amplitudeZ).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            super.onAnimationEnd(animation)
            viewRef.get()?.runSwingAnimation(-amplitudeZ, period)
        }
    })
}

fun View.scaleToFitAnimatedAndBack(
    timeUp: Long,
    timeDelay: Long,
    timeDown: Long,
    scaleRate: Float = 1f,
    onEnd: Func = {}
) {
    val animPair = calsScaleToFitParentAnimation(this, scaleRate)
    val animScaleUp = animPair.first
    val animScaleDown = animPair.second.apply {
        duration = timeDown
    }
    val view = this
    animScaleUp.duration = timeUp
    animScaleUp.fillAfter = true
    animScaleUp.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) = Unit

        override fun onAnimationEnd(animation: Animation?) {
            animScaleDown.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) = Unit

                override fun onAnimationEnd(animation: Animation?) {
                    onEnd.invoke()
                }

                override fun onAnimationRepeat(animation: Animation?) = Unit
            })
            if (view.isShown) {
                view.postDelayed({
                    view.startAnimation(animScaleDown)
                }, timeDelay)
            } else {
                view.animation?.cancel()
                onEnd.invoke()
            }
        }

        override fun onAnimationRepeat(animation: Animation?) = Unit
    })
    this.startAnimation(animScaleUp)
}

private const val half = 0.5f
private const val full = 1.0f

private fun calsScaleToFitParentAnimation(
    view: View,
    scaleRate: Float = 0f
): Pair<ScaleAnimation, ScaleAnimation> {
    val parent = (view.parent as View)
    val screenW = parent.width
    val screenH = parent.height
    val yBelowCenter = view.bottom - view.height / 2 - screenH / 2
    val xBelowCenter = view.right - view.width / 2 - screenW / 2
    val scaleX = screenW.toFloat() / view.width
    val scaleY = screenH.toFloat() / view.height
    val scaleFactor = min(scaleX, scaleY) * scaleRate
    val pivotY = half + yBelowCenter.toFloat() / (screenH / 2)
    val pivotX = half + xBelowCenter.toFloat() / (screenW / 2)
    val scaleUpAnimation = ScaleAnimation(
        full, scaleFactor,
        full, scaleFactor,
        Animation.RELATIVE_TO_SELF, pivotX,
        Animation.RELATIVE_TO_SELF, pivotY
    )
    val scaleDownAnimation = ScaleAnimation(
        scaleFactor, full,
        scaleFactor, full,
        Animation.RELATIVE_TO_SELF, pivotX,
        Animation.RELATIVE_TO_SELF, pivotY
    )
    return Pair(scaleUpAnimation, scaleDownAnimation)
}