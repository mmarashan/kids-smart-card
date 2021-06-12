package ru.volgadev.article_galery.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.volgadev.article_galery.domain.ArticleGalleryInteractor
import ru.volgadev.article_galery.domain.ArticleGalleryInteractorImpl
import ru.volgadev.article_galery.presentation.ArticleGalleryViewModel
import ru.volgadev.common.FeatureToggles
import ru.volgadev.core.musicplayer.api.MusicPlayer
import ru.volgadev.core.musicplayer.api.MusicPlayerProvider

val articleGalleryFeatureModule = module {
    single<ArticleGalleryInteractor> {
        ArticleGalleryInteractorImpl(
            articleRepository = get(),
            musicRepository = get(),
            musicPlayer = get(),
            cardPlayer = get(),
            isBackgroundMusicEnabled = FeatureToggles.ENABLE_BACKGROUND_MUSIC
        )
    }
    factory<MusicPlayer> { MusicPlayerProvider.createPlayer(context = get()) }
    viewModel { ArticleGalleryViewModel(get()) }
}