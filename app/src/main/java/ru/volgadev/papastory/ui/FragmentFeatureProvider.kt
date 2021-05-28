package ru.volgadev.papastory.ui

import androidx.fragment.app.Fragment
import ru.volgadev.article_galery.presentation.ArticleGalleryFragment
import ru.volgadev.cabinet_feature.presentation.CabinetFragment

enum class AppFragment {
    GALLERY_FRAGMENT, CABINET_FRAGMENT
}

class FragmentFeatureProvider {

    fun getNextFragmentFeature(code: AppFragment): Fragment {
        return when (code) {
            AppFragment.GALLERY_FRAGMENT -> ArticleGalleryFragment()
            AppFragment.CABINET_FRAGMENT -> CabinetFragment()
        }
    }

    companion object {
        private val FULLSCREEN_FRAGMENTS_CLASS_NAMES = setOf<String>()

        fun isFullscreen(fragment: Fragment): Boolean {
            return fragment::class.java.canonicalName in FULLSCREEN_FRAGMENTS_CLASS_NAMES
        }
    }
}