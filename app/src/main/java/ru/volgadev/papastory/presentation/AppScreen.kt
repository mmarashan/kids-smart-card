package ru.volgadev.papastory.presentation

import androidx.fragment.app.Fragment
import ru.volgadev.article_galery.presentation.ArticleGalleryFragment
import ru.volgadev.cabinet_feature.presentation.CabinetFragment

interface AppScreen {
    fun getScreen(): Fragment
}

object CardsScreen : AppScreen {
    override fun getScreen(): Fragment = ArticleGalleryFragment()

}

object CabinetScreen : AppScreen {
    override fun getScreen(): Fragment = CabinetFragment()
}