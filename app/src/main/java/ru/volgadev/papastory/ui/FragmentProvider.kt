package ru.volgadev.papastory.ui

import androidx.fragment.app.Fragment
import ru.volgadev.article_galery.ui.ArticleGalleryFragment
import ru.volgadev.article_page.ArticlePageFragment

enum class AppFragment {
    GALERY_FRAGMENT, ARTICLE_PAGE_FRAGMENT
}

class FragmentProvider {

    companion object {

        private val FULLSCREEN_FRAGMENTS_CLASS_NAMES =
            // TODO: решить проблему с backstack
            // listOf(ArticlePageFragment::class.java.canonicalName)
            listOf<String>()

        private val articleGalleryFragment by lazy { ArticleGalleryFragment.newInstance() }

        fun get(code: AppFragment): Fragment {
            return when (code) {
                AppFragment.GALERY_FRAGMENT -> {
                    articleGalleryFragment
                }
                AppFragment.ARTICLE_PAGE_FRAGMENT -> {
                    ArticlePageFragment.newInstance()
                }
            }
        }

        fun isFullscreen(fragment: Fragment): Boolean {
            return fragment::class.java.canonicalName in FULLSCREEN_FRAGMENTS_CLASS_NAMES
        }
    }
}