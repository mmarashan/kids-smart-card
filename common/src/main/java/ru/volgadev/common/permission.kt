package ru.volgadev.common

import android.content.Context
import android.content.pm.PackageManager

import androidx.core.app.ActivityCompat

fun Context.isPermissionGranted(permission: String): Boolean {
    val permissionCheck = ActivityCompat.checkSelfPermission(this, permission)
    return permissionCheck == PackageManager.PERMISSION_GRANTED
}