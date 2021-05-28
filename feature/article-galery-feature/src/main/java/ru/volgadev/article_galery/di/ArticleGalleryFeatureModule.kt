package ru.volgadev.article_galery.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.volgadev.article_galery.domain.ArticleGalleryInteractor
import ru.volgadev.article_galery.domain.ArticleGalleryInteractorImpl
import ru.volgadev.article_galery.presentation.ArticleGalleryViewModel
import ru.volgadev.common.FeatureToggles

val articleGalleryFeatureModule = module {
    single<ArticleGalleryInteractor> {
        ArticleGalleryInteractorImpl(
            articleRepository = get(),
            musicRepository = get(),
            isBackgroundMusicEnabled = FeatureToggles.ENABLE_BACKGROUND_MUSIC
        )
    }
    viewModel { ArticleGalleryViewModel(get()) }
}