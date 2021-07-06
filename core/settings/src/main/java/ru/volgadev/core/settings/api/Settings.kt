package ru.volgadev.core.settings.api

import ru.volgadev.core.settings.impl.SettingsImpl

interface Settings {

    fun baseUrl(): String

    fun isBackgroundMusicEnabled(): Boolean

    companion object {

        fun get(): Settings {
            return SettingsImpl()
        }
    }
}