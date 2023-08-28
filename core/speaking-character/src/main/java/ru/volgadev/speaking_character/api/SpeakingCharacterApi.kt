package ru.volgadev.speaking_character.api

import android.app.Activity
import android.content.Context
import ru.volgadev.speaking_character.impl.SpeakingCharacterApiImpl

interface SpeakingCharacterApi {

    fun show(
        activity: Activity,
        character: Character,
        utteranceText: String? = null,
        showTimeMs: Long
    )

    companion object {
        fun get(context: Context): SpeakingCharacterApi {
            return SpeakingCharacterApiImpl(context)
        }
    }
}
