package ru.volgadev.core.musicplayer.impl.ext

import android.net.Uri
import com.google.android.exoplayer2.MediaItem
import ru.volgadev.core.musicplayer.api.PlayerTrack
import java.io.File

internal fun PlayerTrack.toMediaItem(): MediaItem? {
    val path = if (file != null && file.isExistsNonEmptyFile()) file.path else remoteUri?.toString()
    val uri: Uri = try {
        Uri.parse(path)
    } catch (e: NullPointerException) {
        null
    } ?: return null
    return MediaItem.Builder().setUri(uri).setMediaId(id).build()
}

internal fun File.isExistsNonEmptyFile(): Boolean {
    return isFile && exists() && length() > 0
}