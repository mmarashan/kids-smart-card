package ru.volgadev.speaking_character.api

import androidx.annotation.DrawableRes

data class Character(
    @DrawableRes
    val imageRes: Int,
    val textBound: TextBound,
    var size: CharacterSize,
    var availableDirections: Set<Direction> = Direction.values().toSet()
)

data class TextBound(val x0: Float, val y0: Float, val x1: Float, val y1: Float)

enum class Direction {
    FROM_TOP, FROM_BOTTOM, FROM_LEFT, FROM_RIGHT, FROM_TOP_LEFT, FROM_BOTTOM_LEFT, FROM_TOP_RIGHT, FROM_BOTTOM_RIGHT
}

data class CharacterSize(val widthDp: Float, val heightDp: Float)