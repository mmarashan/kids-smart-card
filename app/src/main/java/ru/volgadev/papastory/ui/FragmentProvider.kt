package ru.volgadev.papastory.ui

import android.app.Activity
import androidx.fragment.app.Fragment
import ru.volgadev.article_galery.api.ArticleGalleryFeatureApi
import ru.volgadev.article_page.api.ArticlePageFeatureApi
import ru.volgadev.article_page.presentation.ArticlePageFragment
import ru.volgadev.cabinet_feature.api.CabinetFeatureApi
import ru.volgadev.papastory.KidsCardApplication
import javax.inject.Inject

enum class AppFragment {
    GALLERY_FRAGMENT, ARTICLE_PAGE_FRAGMENT, CABINET_FRAGMENT
}

class FragmentProvider(activity: Activity) {

    @Inject
    lateinit var articleGalleryFeatureApi: ArticleGalleryFeatureApi

    @Inject
    lateinit var articlePageFeatureApi: ArticlePageFeatureApi

    @Inject
    lateinit var cabinetFeatureApi: CabinetFeatureApi

    init {
        ((activity.application) as KidsCardApplication).appComponent.inject(this)
    }

    fun get(code: AppFragment): Fragment = when (code) {
        AppFragment.GALLERY_FRAGMENT -> articleGalleryFeatureApi.getFragment()
        AppFragment.ARTICLE_PAGE_FRAGMENT -> articlePageFeatureApi.getFragment()
        AppFragment.CABINET_FRAGMENT -> cabinetFeatureApi.getFragment()
    }

    companion object {
        private val FULLSCREEN_FRAGMENTS_CLASS_NAMES =
            setOf(ArticlePageFragment::class.java.canonicalName)

        fun isFullscreen(fragment: Fragment): Boolean {
            return fragment::class.java.canonicalName in FULLSCREEN_FRAGMENTS_CLASS_NAMES
        }
    }
}