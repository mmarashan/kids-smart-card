package ru.volgadev.common

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.annotation.AnyThread
import ru.volgadev.common.log.Logger

@AnyThread
class BackgroundMediaPlayer : MediaPlayer() {

    private val logger = Logger.get("BackgroundMediaPlayer")

    private val backgroundHandler by lazy {
        val handlerThread = HandlerThread("background_media_player").apply { start() }
        Handler(handlerThread.looper).also { logger.debug("init") }
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

    fun playAudio(context: Context, path: String) {
        logger.debug("Prepare. Set data source $path")
        setDataSource(context, Uri.parse(path))
        prepareAsync()
        setOnPreparedListener {
            start()
        }
    }

}