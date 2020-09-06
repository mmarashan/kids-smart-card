package ru.volgadev.article_galery.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.music_data.repository.MusicRepository

class ArticleGalleryViewModel(private val articleRepository: ArticleRepository,
                              private val musicRepository: MusicRepository) : ViewModel() {

    val articles: LiveData<ArrayList<Article>> = articleRepository.articles().asLiveData()

    val tracks = musicRepository.musicTracks().asLiveData()
}