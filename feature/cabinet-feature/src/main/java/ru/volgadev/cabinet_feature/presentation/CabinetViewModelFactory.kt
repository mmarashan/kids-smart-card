package ru.volgadev.cabinet_feature.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.volgadev.article_repository.domain.ArticleRepository

internal object CabinetViewModelFactory : ViewModelProvider.Factory {

    private var articleRepository: ArticleRepository? = null

    fun inject(articleRepository: ArticleRepository) {
        this.articleRepository = articleRepository
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val articleRepository = articleRepository
        checkNotNull(articleRepository) { "articleRepository was not initialized!" }
        return CabinetViewModel(articleRepository) as T
    }

    fun clear() {
        articleRepository = null
    }
}