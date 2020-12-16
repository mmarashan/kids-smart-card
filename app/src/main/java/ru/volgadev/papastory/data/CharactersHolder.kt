package ru.volgadev.papastory.data

import android.content.Context
import ru.volgadev.papastory.R
import ru.volgadev.speaking_character.Character
import ru.volgadev.speaking_character.CharacterSize
import ru.volgadev.speaking_character.Directon
import ru.volgadev.speaking_character.TextBound

class CharactersHolder(private val context: Context) {

    fun getRandom(): Character {
        return when ((0..2).random()){
            0 -> gingerCat
            1 -> mole
            2 -> whale
            else -> gingerCat
        }
    }

    val gingerCat by lazy {
        Character(
            "cat",
            context.resources.getDrawable(R.drawable.ginger_cat, null),
            TextBound(0.35f, 0.09f, 0.88f, 0.35f),
            CharacterSize(164f, 164f),
            setOf(
                Directon.FROM_BOTTOM,
                Directon.FROM_TOP,
                Directon.FROM_BOTTOM_LEFT,
                Directon.FROM_TOP_LEFT
            )
        )
    }

    val mole by lazy {
        Character(
            "cat",
            context.resources.getDrawable(R.drawable.mole, null),
            TextBound(0.35f, 0.09f, 0.88f, 0.35f),
            CharacterSize(164f, 164f),
            setOf(
                Directon.FROM_BOTTOM,
                Directon.FROM_BOTTOM_RIGHT,
                Directon.FROM_BOTTOM_LEFT
            )
        )
    }

    val whale by lazy {
        Character(
            "cat",
            context.resources.getDrawable(R.drawable.whale, null),
            TextBound(0.35f, 0.09f, 0.88f, 0.35f),
            CharacterSize(164f, 164f),
            setOf(
                Directon.FROM_BOTTOM,
                Directon.FROM_LEFT,
                Directon.FROM_BOTTOM_LEFT,
                Directon.FROM_TOP_LEFT
            )
        )
    }
}