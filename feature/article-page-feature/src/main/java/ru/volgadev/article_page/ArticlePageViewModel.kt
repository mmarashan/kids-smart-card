package ru.volgadev.article_page

import androidx.annotation.AnyThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.log.Logger

class ArticlePageViewModel(private val articleRepository: ArticleRepository) : ViewModel() {

    private val logger = Logger.get("ArticlePageViewModel")

    val _article = MutableLiveData<Article>()
    val article: LiveData<Article> = _article

    @AnyThread
    fun onChooseArticle(id: Long) {
        viewModelScope.launch {
            val article = articleRepository.getArticle(id)
            if (article!=null) {
                logger.debug("Use article ${article.id}")
                _article.postValue(article)
            } else {
                logger.error("Article $id not found")
            }
        }
    }
}