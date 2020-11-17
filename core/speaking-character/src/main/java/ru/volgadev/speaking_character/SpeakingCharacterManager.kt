package ru.volgadev.speaking_character

import android.content.Context
import android.view.View
import android.widget.ImageView
import ru.volgadev.common.log.Logger

class SpeakingCharacterManager(val context: Context) {

    private val logger = Logger.get("SpeakingCharacterManager")

    fun show(parent: View, character: Character, showTimeMs: Long) {
        logger.debug("show()")

        val imageView = ImageView(context)
        imageView.setImageBitmap(character.bitmap)
    }

    private companion object {

    }
}