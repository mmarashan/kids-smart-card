package ru.volgadev.cardgallery.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.volgadev.cardgallery.domain.ArticleGalleryInteractor
import ru.volgadev.cardgallery.domain.ArticleGalleryInteractorImpl
import ru.volgadev.cardgallery.presentation.CardGalleryViewModel
import ru.volgadev.core.musicplayer.api.MusicPlayer
import ru.volgadev.core.musicplayer.api.MusicPlayerProvider
import ru.volgadev.core.settings.api.Settings

val articleGalleryFeatureModule = module {
    single<ArticleGalleryInteractor> {
        ArticleGalleryInteractorImpl(
            cardRepository = get(),
            musicRepository = get(),
            musicPlayer = get(),
            cardPlayer = get(),
            isBackgroundMusicEnabled = get<Settings>().isBackgroundMusicEnabled()
        )
    }
    factory<MusicPlayer> { MusicPlayerProvider.createPlayer(context = get()) }
    viewModel { CardGalleryViewModel(get()) }
}