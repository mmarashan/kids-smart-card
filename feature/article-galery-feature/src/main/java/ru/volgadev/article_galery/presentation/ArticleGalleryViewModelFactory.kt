package ru.volgadev.article_galery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.volgadev.article_data.domain.ArticleRepository
import ru.volgadev.music_data.domain.MusicRepository

internal object ArticleGalleryViewModelFactory : ViewModelProvider.Factory {

    private var articleRepository: ArticleRepository? = null
    private var musicRepository: MusicRepository? = null

    fun inject(articleRepository: ArticleRepository, musicRepository: MusicRepository) {
        this.articleRepository = articleRepository
        this.musicRepository = musicRepository
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val articleRepository = articleRepository
        val musicRepository = musicRepository
        checkNotNull(articleRepository) { "articleRepository was not initialized!" }
        checkNotNull(musicRepository) { "musicRepository was not initialized!" }
        return ArticleGalleryViewModel(articleRepository, musicRepository) as T
    }

    fun clear() {
        articleRepository = null
        musicRepository = null
    }
}