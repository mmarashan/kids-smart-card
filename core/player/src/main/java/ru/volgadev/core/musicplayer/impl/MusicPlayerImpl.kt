package ru.volgadev.core.musicplayer.impl

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import ru.volgadev.core.musicplayer.api.MusicPlayer
import ru.volgadev.core.musicplayer.api.PlayerListener
import ru.volgadev.core.musicplayer.api.PlayerTrack
import ru.volgadev.core.musicplayer.impl.ext.toMediaItem

internal class MusicPlayerImpl(
    private val context: Context
) : MusicPlayer {

    private val handler by lazy {
        val handlerThread = HandlerThread("music_player_$this")
            .apply { start() }
        Handler(handlerThread.looper)
    }

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            playerListener?.onIsPlayingChanged(isPlaying, getCurrent())
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            Log.e(TAG, "ExoPlayer error, ${error.sourceException.message}")
        }
    }

    private val player: SimpleExoPlayer by lazy {
        Log.d(TAG, "Create player")
        SimpleExoPlayer
            .Builder(context.applicationContext)
            .setLooper(handler.looper)
            .build().apply {
                addListener(listener)
            }

    }

    private val playlist = ArrayList<PlayerTrack>()

    private var playerListener: PlayerListener? = null

    override fun play() {
        Log.d(TAG, "play")
        handler.post {
            player.play()
        }
    }

    override fun pause() {
        Log.d(TAG, "pause")
        handler.post {
            player.pause()
        }
    }

    override fun stop() {
        Log.d(TAG, "stop")
        handler.post {
            player.stop()
        }
    }

    override fun setListener(listener: PlayerListener?) {
        playerListener = listener
    }

    override fun setPlaylist(playlist: Collection<PlayerTrack>) {
        Log.d(TAG, "setPlaylist $playlist")
        this.playlist.clear()
        this.playlist.addAll(playlist)

        val mediaItems = playlist.mapNotNull { it.toMediaItem() }
        handler.post {
            player.setMediaItems(mediaItems, true)
            player.prepare()
        }
    }

    override fun playNow(track: PlayerTrack) {
        Log.d(TAG, "playNow $track")
        handler.post {
            val itemPositionInPlaylist = playlist.indexOf(track)
            if (itemPositionInPlaylist >= 0) {
                player.seekTo(itemPositionInPlaylist, 0)
            } else {
                Log.d(TAG, "playNow $track; track not in playlist!")
            }
            play()
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
        playlist.clear()
        handler.post {
            player.release()
            handler.looper.quitSafely()
        }
    }

    private companion object {
        const val TAG = "MusicPlayerImpl"
    }
}