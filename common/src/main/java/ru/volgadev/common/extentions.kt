package ru.volgadev.common

import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

fun Context.applicationDataDir(): String {
    val p: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
    return p.applicationInfo.dataDir+"/"
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