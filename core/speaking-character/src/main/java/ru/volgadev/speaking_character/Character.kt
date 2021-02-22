package ru.volgadev.speaking_character

import android.graphics.drawable.Drawable

data class TextBound(val x0: Float, val y0: Float, val x1: Float, val y1: Float)

data class Character(
    val name: String,
    val drawable: Drawable,
    val textBound: TextBound,
    var size: CharacterSize,
    var availableDirections: Set<Directon> = Directon.values().toSet()
)

enum class Directon {
    FROM_TOP, FROM_BOTTOM, FROM_LEFT, FROM_RIGHT, FROM_TOP_LEFT, FROM_BOTTOM_LEFT, FROM_TOP_RIGHT, FROM_BOTTOM_RIGHT
}

data class CharacterSize(val widthDp: Float, val heightDp: Float)