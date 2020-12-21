package ru.volgadev.papastory.ui

import androidx.fragment.app.Fragment
import ru.volgadev.article_galery.ui.ArticleGalleryFragment
import ru.volgadev.article_page.ArticlePageFragment
import ru.volgadev.cabinet_feature.CabinetFragment

enum class AppFragment {
    GALERY_FRAGMENT, ARTICLE_PAGE_FRAGMENT, CABINET_FRAGMENT
}

object FragmentProvider {

    private val FULLSCREEN_FRAGMENTS_CLASS_NAMES =
        listOf(ArticlePageFragment::class.java.canonicalName)

    private val articleGalleryFragment by lazy { ArticleGalleryFragment.newInstance() }
    private val cabinetFragment by lazy { CabinetFragment.newInstance() }

    @JvmStatic
    fun warm() {
        articleGalleryFragment
    }

    @JvmStatic
    fun get(code: AppFragment): Fragment {
        return when (code) {
            AppFragment.GALERY_FRAGMENT -> {
                articleGalleryFragment
            }
            AppFragment.ARTICLE_PAGE_FRAGMENT -> {
                ArticlePageFragment.newInstance()
            }
            AppFragment.CABINET_FRAGMENT -> {
                cabinetFragment
            }
        }
    }

    @JvmStatic
    fun isFullscreen(fragment: Fragment): Boolean {
        return fragment::class.java.canonicalName in FULLSCREEN_FRAGMENTS_CLASS_NAMES
    }
}