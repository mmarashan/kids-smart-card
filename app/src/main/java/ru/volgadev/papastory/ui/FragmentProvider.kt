package ru.volgadev.papastory.ui

import androidx.fragment.app.Fragment
import ru.volgadev.article_galery.ui.ArticleGalleryFragment
import ru.volgadev.article_page.ArticlePageFragment
import ru.volgadev.cabinet_feature.CabinetFragment

enum class AppFragment {
    GALLERY_FRAGMENT, ARTICLE_PAGE_FRAGMENT, CABINET_FRAGMENT
}

object FragmentProvider {

    private val FULLSCREEN_FRAGMENTS_CLASS_NAMES =
        listOf(ArticlePageFragment::class.java.canonicalName)

    @JvmStatic
    fun get(code: AppFragment): Fragment {
        return when (code) {
            AppFragment.GALLERY_FRAGMENT -> {
                ArticleGalleryFragment()
            }
            AppFragment.ARTICLE_PAGE_FRAGMENT -> {
                ArticlePageFragment()
            }
            AppFragment.CABINET_FRAGMENT -> {
                CabinetFragment()
            }
        }
    }

    @JvmStatic
    fun isFullscreen(fragment: Fragment): Boolean {
        return fragment::class.java.canonicalName in FULLSCREEN_FRAGMENTS_CLASS_NAMES
    }
}