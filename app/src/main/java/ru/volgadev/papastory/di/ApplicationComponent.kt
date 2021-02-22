package ru.volgadev.papastory.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.music_data.repository.MusicRepository
import ru.volgadev.papastory.ui.FragmentProvider

@Component(
    modules = [
        ArticleRepositoryModule::class,
        MusicRepositoryModule::class,
        PaymentManagerModule::class,
        ArticleGalleryFeatureModule::class
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