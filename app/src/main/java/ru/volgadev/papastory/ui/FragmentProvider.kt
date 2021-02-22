package ru.volgadev.papastory.ui

import android.app.Activity
import androidx.fragment.app.Fragment
import ru.volgadev.article_galery.api.ArticleGalleryFeatureApi
import ru.volgadev.article_page.ArticlePageFragment
import ru.volgadev.cabinet_feature.CabinetFragment
import ru.volgadev.papastory.KidsCardApplication
import javax.inject.Inject

enum class AppFragment {
    GALLERY_FRAGMENT, ARTICLE_PAGE_FRAGMENT, CABINET_FRAGMENT
}

class FragmentProvider(activity: Activity) {

    @Inject
    lateinit var articleGalleryFeatureApi: ArticleGalleryFeatureApi

    init {
        ((activity.application) as KidsCardApplication).appComponent.inject(this)
    }

    fun get(code: AppFragment): Fragment = when (code) {
        AppFragment.GALLERY_FRAGMENT -> articleGalleryFeatureApi.getFragment()
        AppFragment.ARTICLE_PAGE_FRAGMENT -> ArticlePageFragment()
        AppFragment.CABINET_FRAGMENT -> CabinetFragment()
    }

    companion object {
        private val FULLSCREEN_FRAGMENTS_CLASS_NAMES =
            setOf(ArticlePageFragment::class.java.canonicalName)

        fun isFullscreen(fragment: Fragment): Boolean {
            return fragment::class.java.canonicalName in FULLSCREEN_FRAGMENTS_CLASS_NAMES
        }
    }
}