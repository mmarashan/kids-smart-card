package ru.volgadev.core.musicplayer.impl

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import com.google.android.exoplayer2.SimpleExoPlayer
import ru.volgadev.core.musicplayer.api.MusicPlayer
import ru.volgadev.core.musicplayer.api.PlayerTrack
import ru.volgadev.core.musicplayer.impl.ext.toMediaItem

internal class MusicPlayerImpl(
    private val context: Context
) : MusicPlayer {

    private val handler by lazy {
        val handlerThread = HandlerThread("background_music_player $this")
            .apply { start() }
        Handler(handlerThread.looper)
    }

    private val player by lazy {
        SimpleExoPlayer
            .Builder(context)
            .setLooper(handler.looper)
            .build()
    }

    private val playlist = ArrayList<PlayerTrack>()

    override fun play() {
        if (isPlaying()) return
        handler.post {
            player.prepare()
            player.play()
        }
    }

    override fun pause() {
        handler.post {
            player.pause()
        }
    }

    override fun stop() {
        handler.post {
            player.stop()
        }
    }

    override fun setPlaylist(playlist: Collection<PlayerTrack>) {
        this.playlist.clear()
        this.playlist.addAll(playlist)

        val mediaItems = playlist.mapNotNull { it.toMediaItem() }
        handler.post {
            player.setMediaItems(mediaItems, true)
        }
    }

    override fun playNow(track: PlayerTrack) {
        handler.post {
            val itemPositionInPlaylist = playlist.indexOf(track)
            if (itemPositionInPlaylist >= 0) {
                player.seekTo(itemPositionInPlaylist, 0)
            } else {
                track.toMediaItem()?.let {
                    player.addMediaItem(0, it)
                    player.seekTo(0, 0)
                }
            }
        }
    }

    override fun getCurrent(): PlayerTrack? {
        val currentItemId = player.currentMediaItem?.mediaId ?: return null
        return playlist.firstOrNull { it.id == currentItemId }
    }

    override fun next() {
        handler.post {
            player.next()
        }
    }

    override fun previous() {
        handler.post {
            player.previous()
        }
    }

    override fun setVolume(volume: Float) {
        handler.post {
            player.volume = volume
        }
    }

    override fun isPlaying(): Boolean = player.isPlaying

    override fun release() {
        handler.post { player.release() }
    }
}