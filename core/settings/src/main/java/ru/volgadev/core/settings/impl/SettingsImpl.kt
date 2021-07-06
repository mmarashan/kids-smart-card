package ru.volgadev.core.settings.impl

import ru.volgadev.core.settings.api.Settings

internal class SettingsImpl : Settings {

    override fun isBackgroundMusicEnabled(): Boolean = true

    override fun baseUrl(): String =
        "https://raw.githubusercontent.com/mmarashan/psdata/release-0.7"
}