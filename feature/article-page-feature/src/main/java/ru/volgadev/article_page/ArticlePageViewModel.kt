package ru.volgadev.article_page

import androidx.annotation.AnyThread
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.volgadev.common.log.Logger
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.repository.ArticleRepository

class ArticlePageViewModel(private val articleRepository: ArticleRepository) : ViewModel() {

    private val logger = Logger.get("ArticlePageViewModel")

    val _article = MutableLiveData<Article>()
    val article: LiveData<Article> = _article

    @AnyThread
    fun onChooseArticle(id: Long){
        viewModelScope.launch {
            val article = articleRepository.getArticle(id)
            _article.postValue(article)
        }
    }
}