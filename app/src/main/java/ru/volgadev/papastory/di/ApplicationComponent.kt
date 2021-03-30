package ru.volgadev.papastory.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.volgadev.papastory.di.core.ArticleRepositoryModule
import ru.volgadev.papastory.di.core.MusicRepositoryModule
import ru.volgadev.papastory.di.core.PaymentManagerModule
import ru.volgadev.papastory.di.feature.ArticleGalleryFeatureModule
import ru.volgadev.papastory.di.feature.ArticlePageFeatureModule
import ru.volgadev.papastory.di.feature.CabinetFeatureModule
import ru.volgadev.papastory.ui.FragmentFeatureProvider

@Component(
    modules = [
        ArticleRepositoryModule::class,
        MusicRepositoryModule::class,
        PaymentManagerModule::class,
        ArticleGalleryFeatureModule::class,
        CabinetFeatureModule::class,
        ArticlePageFeatureModule::class
    ]
)
interface ApplicationComponent {

    @Component.Factory
    interface Factory {
        /* With @BindsInstance, the Context passed in will be available in the graph */
        fun create(@BindsInstance context: Context): ApplicationComponent
    }

    fun inject(fragmentFeatureProvider: FragmentFeatureProvider)
}