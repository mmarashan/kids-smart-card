package ru.volgadev.papastory.ui

import android.app.Activity
import androidx.fragment.app.Fragment
import dagger.Lazy
import ru.sberdevices.module_injector.BaseAPI
import ru.sberdevices.module_injector.BaseDependencies
import ru.sberdevices.module_injector.ComponentHolder
import ru.volgadev.article_galery.api.ArticleGalleryFeatureComponentHolder
import ru.volgadev.article_galery.api.ArticleGalleryFeatureDependencies
import ru.volgadev.cabinet_feature.api.CabinetFeatureComponentHolder
import ru.volgadev.cabinet_feature.api.CabinetFeatureDependencies
import ru.volgadev.papastory.KidsCardApplication
import javax.inject.Inject

enum class AppFragment {
    GALLERY_FRAGMENT, CABINET_FRAGMENT
}

class FragmentFeatureProvider(activity: Activity) {

    private var lastFeatureComponentHolder: ComponentHolder<BaseAPI, BaseDependencies>? = null

    @Inject
    lateinit var articleGalleryFeatureDependencies: Lazy<ArticleGalleryFeatureDependencies>

    @Inject
    lateinit var cabinetFeatureDependencies: Lazy<CabinetFeatureDependencies>

    @Inject
    lateinit var articleGalleryFeatureHolder: Lazy<ArticleGalleryFeatureComponentHolder>

    @Inject
    lateinit var cabinetFeatureComponentHolder: Lazy<CabinetFeatureComponentHolder>

    init {
        ((activity.application) as KidsCardApplication).appComponent.inject(this)
    }

    fun getNextFragmentFeature(code: AppFragment): Fragment {
        lastFeatureComponentHolder?.clear()
        lastFeatureComponentHolder = (when (code) {
            AppFragment.GALLERY_FRAGMENT -> articleGalleryFeatureHolder.get().apply {
                init(articleGalleryFeatureDependencies.get())
            }
            AppFragment.CABINET_FRAGMENT -> cabinetFeatureComponentHolder.get().apply {
                init(cabinetFeatureDependencies.get())
            }
        }) as ComponentHolder<BaseAPI, BaseDependencies>

        return when (code) {
            AppFragment.GALLERY_FRAGMENT -> articleGalleryFeatureHolder.get().get().getFragment()
            AppFragment.CABINET_FRAGMENT -> cabinetFeatureComponentHolder.get().get().getFragment()
        }
    }

    companion object {
        private val FULLSCREEN_FRAGMENTS_CLASS_NAMES = setOf<String>()

        fun isFullscreen(fragment: Fragment): Boolean {
            return fragment::class.java.canonicalName in FULLSCREEN_FRAGMENTS_CLASS_NAMES
        }
    }
}