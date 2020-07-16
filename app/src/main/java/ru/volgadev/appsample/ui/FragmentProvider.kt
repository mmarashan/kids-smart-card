package ru.volgadev.appsample.ui

import androidx.fragment.app.Fragment
import ru.volgadev.article_galery.ui.ArticleGaleryFragment
import ru.volgadev.article_page.ArticlePageFragment

enum class AppFragment {
    GALERY_FRAGMENT, ARTICLE_PAGE_FRAGMENT
}

class FragmentProvider {

    companion object {

        private val FULLSCREEN_FRAGMENTS_CLASS_NAMES =
            listOf(ArticlePageFragment.javaClass.canonicalName)

        private val articleGaleryFragment by lazy { ArticleGaleryFragment.newInstance() }

        fun get(code: AppFragment): Fragment {
            when (code) {
                AppFragment.GALERY_FRAGMENT -> {
                    return articleGaleryFragment
                }
                AppFragment.ARTICLE_PAGE_FRAGMENT -> {
                    return ArticlePageFragment.newInstance()
                }
            }
        }

        fun isFullscreen(fragment: Fragment): Boolean {
            return fragment.javaClass.canonicalName in FULLSCREEN_FRAGMENTS_CLASS_NAMES
        }
    }
}