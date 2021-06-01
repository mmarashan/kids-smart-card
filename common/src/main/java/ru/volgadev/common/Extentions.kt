package ru.volgadev.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.io.File
import java.lang.ref.WeakReference
import java.net.MalformedURLException
import java.net.URL
import kotlin.math.min
import kotlin.math.roundToInt


fun Context.applicationDataDir(): String {
    val p: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
    return p.applicationInfo.dataDir + "/"
}

fun Context.isPermissionGranted(permission: String): Boolean {
    val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
    return permissionCheck == PackageManager.PERMISSION_GRANTED
}

fun Activity.hideNavBar() {
    window.hideNavBar()
}

fun Activity.showNavBar() {
    window.showNavBar()
}

fun Window.hideNavBar() {
    val decorView: View = this.decorView
    val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    decorView.systemUiVisibility = uiOptions
}

fun Window.showNavBar() {
    val decorView: View = this.decorView
    val uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    decorView.systemUiVisibility = uiOptions
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


fun View.runLevitateAnimation(amplitudeY: Float, period: Long) {
    val viewRef = WeakReference<View>(this)
    val interpolator: TimeInterpolator = DecelerateInterpolator()
    val animator = this.animate().apply {
        setInterpolator(interpolator)
        setDuration(period / 2)
    }
    animator.translationYBy(amplitudeY).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            super.onAnimationEnd(animation)
            viewRef.get()?.runLevitateAnimation(-amplitudeY, period)
        }
    })
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
    onEnd: () -> Unit = {}
) {
    val animPair = scaleToFitParentAnimation(this, scaleRate)
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

private fun scaleToFitParentAnimation(
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

fun Context.getScreenSize(): Pair<Int, Int> {
    val displayMetrics = resources.displayMetrics
    return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
}

fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
    observeForever(object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

fun String.isValidUrlString(): Boolean {
    try {
        URL(this)
        return URLUtil.isValidUrl(this) && Patterns.WEB_URL.matcher(this)
            .matches()
    } catch (e: MalformedURLException) {
        return false
    }
}

fun File.isExistsNonEmptyFile(): Boolean {
    return isFile && exists() && length() > 0
}

fun Context.dpToPx(dp: Float): Int {
    val metrics = resources.displayMetrics
    return (dp * metrics.density).roundToInt()
}

fun Context.pxToDp(px: Int): Float {
    val metrics = resources.displayMetrics
    return px / metrics.density
}

fun Context.getNavigationBarHeight(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
    if (resourceId > 0) {
        return resources.getDimensionPixelSize(resourceId)
    }
    return 0
}