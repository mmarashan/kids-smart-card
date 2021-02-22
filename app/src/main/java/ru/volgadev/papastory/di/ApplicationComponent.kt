package ru.volgadev.papastory.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.volgadev.papastory.ui.FragmentProvider

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

    fun inject(fragmentProvider: FragmentProvider)
}