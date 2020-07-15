package ru.volgadev.article_galery.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.volgadev.sampledata.model.Article
import ru.volgadev.sampledata.repository.ArticleRepository

class SampleViewModel(private val articleRepository: ArticleRepository) : ViewModel() {

    val articles: LiveData<ArrayList<Article>> = articleRepository.articles().asLiveData()
}