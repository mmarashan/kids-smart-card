package ru.volgadev.papastory.presentation

import androidx.fragment.app.Fragment
import ru.volgadev.cardgallery.presentation.CardGalleryFragment
import ru.volgadev.cabinet_feature.presentation.CabinetFragment

interface AppScreen {
    fun getScreen(): Fragment
}

object CardsScreen : AppScreen {
    override fun getScreen(): Fragment = CardGalleryFragment()

}

object CabinetScreen : AppScreen {
    override fun getScreen(): Fragment = CabinetFragment()
}