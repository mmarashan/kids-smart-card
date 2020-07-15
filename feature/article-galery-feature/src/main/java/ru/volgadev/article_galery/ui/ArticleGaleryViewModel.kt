package ru.volgadev.article_galery.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.repository.ArticleRepository

class ArticleGaleryViewModel(private val articleRepository: ArticleRepository) : ViewModel() {

    val articles: LiveData<ArrayList<Article>> = articleRepository.articles().asLiveData()
}