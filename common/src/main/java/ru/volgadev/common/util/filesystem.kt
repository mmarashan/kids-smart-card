package ru.volgadev.common.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File


private val TAG = "filesystemutil"

fun getDir(dirName: String): File {
    val dir = File(dirName)
    if (!dir.exists()) {
        if (dir.mkdirs()) {
            Log.d(TAG, "Dir created ".plus(dir.absolutePath))
        } else {
            Log.e(TAG, "Dir not created. Check permissions! ".plus(dir.absolutePath))
        }
    } else {
        Log.d(TAG, "Dir exist: ".plus(dir.absolutePath))
    }
    return dir

}

fun readDir(dirName: String): List<File> {
    val dir = File(dirName)
    Log.d(TAG, "Read dir ${dir.absolutePath}")
    var result: List<File> = mutableListOf()
    dir.listFiles()?.forEach { file ->
        result = result.plus(file)
    }
    Log.d(TAG, "Read dir ${dir.absolutePath} : ${dir.listFiles().size} files")
    return result
}

/**
 * Очистить директорию
 * @param dir директория
 */
private fun clearDir(dir: File) {
    val entries = dir.list()
    if (entries != null && entries.isNotEmpty()) {
        for (s in entries) {
            val currentFile = File(dir.path, s)
            currentFile.delete()
        }
    }
}

fun deleteFile(filePath: String) {
    val file = File(filePath)
    if (file.exists()) {
        val result = file.delete()
        Log.d(TAG, "Delete file $filePath $result")
    } else {
        Log.d(TAG, "Delete file $filePath false - not exist")
    }
}

/**
 * Check that file is image
 *
 * @return Bitmap or null
 */
private val okFileExtensions = arrayOf("jpg", "png", "gif", "jpeg")

fun getBitmap(filePath: String): Bitmap? {
    if (accept(filePath)) {
        val options = BitmapFactory.Options()
        val bitmap = BitmapFactory.decodeFile(filePath, options)
        return bitmap
    } else {
        return null
    }
}

fun accept(fileName: String): Boolean {
    val file = File(fileName)
    for (extension in okFileExtensions) {
        if (file.name.toLowerCase().endsWith(extension)) {
            return true
        }
    }
    return false
}
