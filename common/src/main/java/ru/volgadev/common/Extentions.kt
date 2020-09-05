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
import android.media.MediaPlayer
import android.net.Uri
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat


fun Context.applicationDataDir(): String {
    val p: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
    return p.applicationInfo.dataDir+"/"
}

fun Context.isPermissionGranted(permission: String): Boolean {
    val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
    return permissionCheck == PackageManager.PERMISSION_GRANTED
}

fun Drawable.toBitmap(): Bitmap {
    val drawable = this
    if (drawable is BitmapDrawable) {
        return (drawable as BitmapDrawable).bitmap
    }
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun MediaPlayer.playAudio(context: Context, path: String) {
    try {
        Log.d("MediaPlayer.playAudio", "Prepare. Set data source")
        setDataSource(context, Uri.parse(path))
        prepare()
        start()
    } catch (e: Exception) {
        Log.e("MediaPlayer.playAudio", e.message.toString())
    }
}

fun MediaPlayer.mute(mute: Boolean) {
    if (mute) setVolume(0f, 0f)
    else setVolume(1f, 1f)
}

fun Activity.hideNavBar() {
    val decorView: View = window.decorView
    val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    decorView.systemUiVisibility = uiOptions
}

fun Activity.showNavBar() {
    val decorView: View = window.decorView
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


fun View.levitate(amplitudeY: Float, duration: Long) {
        val interpolator: TimeInterpolator = DecelerateInterpolator()
        this.animate().translationYBy(amplitudeY).setDuration(duration)
            .setInterpolator(interpolator).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    levitate(-amplitudeY, duration)
                }
            })
}
