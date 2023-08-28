package ru.volgadev.core.musicplayer.api

import java.io.File
import java.net.URI

interface MusicPlayer {

    fun play()

    fun pause()

    fun stop()

    fun setListener(listener: PlayerListener?)

    fun setPlaylist(playlist: Collection<PlayerTrack>)

    fun playNow(track: PlayerTrack)

    fun getCurrent(): PlayerTrack?

    fun next()

    fun previous()

    fun setVolume(volume: Float)

    fun isPlaying(): Boolean

    fun setRepeat(repeatAll: Boolean)

    fun release()
}

data class PlayerTrack(
    val id: String,
    val file: File? = null,
    val remoteUri: URI? = null
)

interface PlayerListener {
    fun onIsPlayingChanged(isPlaying: Boolean, track: PlayerTrack?)
}