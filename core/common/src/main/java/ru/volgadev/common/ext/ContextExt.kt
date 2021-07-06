package ru.volgadev.common.ext

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

fun Context.applicationDataDir(): String {
    val p: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
    return p.applicationInfo.dataDir + "/"
}

fun Context.isPermissionGranted(permission: String): Boolean {
    val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
    return permissionCheck == PackageManager.PERMISSION_GRANTED
}


fun Context.getScreenSize(): Pair<Int, Int> {
    val displayMetrics = resources.displayMetrics
    return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
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