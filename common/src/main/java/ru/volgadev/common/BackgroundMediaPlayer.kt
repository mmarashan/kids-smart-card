package ru.volgadev.common

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import androidx.annotation.AnyThread
import ru.volgadev.common.log.Logger
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

@AnyThread
class BackgroundMediaPlayer : MediaPlayer() {

    private val logger = Logger.get("BackgroundMediaPlayer")

    private val backgroundHandler by lazy {
        val handlerThread = HandlerThread("background_media_player").apply { start() }
        Handler(handlerThread.looper).also { logger.debug("start background handler") }
    }

    override fun start() {
        logger.debug("start()")
        backgroundHandler.post {
            super.start()
        }
    }

    override fun pause() {
        logger.debug("pause()")
        backgroundHandler.post {
            super.pause()
        }
    }

    override fun release() {
        logger.debug("release()")
        backgroundHandler.post {
            super.release()
        }
    }

    override fun stop() {
        logger.debug("stop()")
        backgroundHandler.post {
            super.stop()
        }
    }

    override fun prepare() {
        logger.debug("prepare()")
        backgroundHandler.post {
            super.prepare()
        }
    }

    override fun prepareAsync() {
        logger.debug("prepareAsync()")
        backgroundHandler.post {
            super.prepareAsync()
        }
    }

    fun stopAndRelease() {
        logger.debug("stopAndRelease()")
        backgroundHandler.post {
            super.stop()
            super.release()
        }
    }

    @Throws(IOException::class, FileNotFoundException::class)
    fun playAudio(context: Context, file: File) {
        logger.debug("Prepare. Set data source file = ${file.path}")
        if (file.isExistsNonEmptyFile()) {
            val uri = Uri.parse(file.path)
            playAudio(context, uri)
        } else {
            throw FileNotFoundException("File nor exist: $file.path")
        }
    }

    @Throws(IOException::class)
    fun playAudio(context: Context, uri: Uri) {
        logger.debug("Prepare. Set data source uri = $uri")
        reset()
        setDataSource(context, uri)
        prepareAsync()
        setOnPreparedListener {
            start()
        }
    }

    fun isPaused(): Boolean {
        return !isPlaying && currentPosition > 0
    }

    fun setMute(mute: Boolean) {
        logger.debug("setMute($mute)")
        backgroundHandler.post {
            if (mute) setVolume(0f, 0f)
            else setVolume(1f, 1f)
        }
    }


}