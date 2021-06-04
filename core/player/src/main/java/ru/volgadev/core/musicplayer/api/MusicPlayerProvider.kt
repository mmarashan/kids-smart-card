package ru.volgadev.core.musicplayer.api

import android.content.Context
import ru.volgadev.core.musicplayer.impl.MusicPlayerImpl

object MusicPlayerProvider {

    fun createPlayer(context: Context): MusicPlayer {
        return MusicPlayerImpl(context)
    }
}