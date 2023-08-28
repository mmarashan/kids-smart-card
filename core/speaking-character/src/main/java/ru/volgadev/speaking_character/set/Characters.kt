package ru.volgadev.speaking_character.set

import ru.volgadev.speaking_character.R
import ru.volgadev.speaking_character.api.Character
import ru.volgadev.speaking_character.api.CharacterSize
import ru.volgadev.speaking_character.api.Direction
import ru.volgadev.speaking_character.api.TextBound

val gingerCat = Character(
    R.drawable.ginger_cat,
    TextBound(0.35f, 0.09f, 0.88f, 0.35f),
    CharacterSize(164f, 164f),
    setOf(
        Direction.FROM_BOTTOM,
        Direction.FROM_BOTTOM_LEFT
    )
)


val mole = Character(
    R.drawable.mole,
    TextBound(0.35f, 0.09f, 0.88f, 0.35f),
    CharacterSize(164f, 164f),
    setOf(
        Direction.FROM_BOTTOM,
        Direction.FROM_BOTTOM_RIGHT,
        Direction.FROM_BOTTOM_LEFT
    )
)
