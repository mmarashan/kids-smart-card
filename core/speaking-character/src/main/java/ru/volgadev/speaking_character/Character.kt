package ru.volgadev.speaking_character

import android.graphics.drawable.Drawable

data class TextBound(val x0: Float, val y0: Float, val x1: Float, val y1: Float)

class Character(val name: String, val drawable: Drawable, val textBound: TextBound)

enum class Directon {
    FROM_TOP, FROM_BOTTOM, FROM_LEFT, FROM_RIGHT
}