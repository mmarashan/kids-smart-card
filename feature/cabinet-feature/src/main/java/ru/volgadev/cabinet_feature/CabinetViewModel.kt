package ru.volgadev.cabinet_feature

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.log.Logger

class CabinetViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val logger = Logger.get("CabinetViewModel")

    private val _categories = MutableLiveData<List<ArticleCategory>>()
    val categories: LiveData<List<ArticleCategory>> = _categories

    init {
        _categories.postValue(
            listOf(
                ArticleCategory(
                    "Alphabet",
                    "English alphabet",
                    "https://www.google.ru/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png",
                    2
                )
            )
        )
    }

    @MainThread
    fun onClickCategory(category: String) {
        logger.debug("onClickCategory $category")
    }

    @MainThread
    fun onClickCategory(article: Article) {
        logger.debug("onClickArticle ${article.title}")
    }

    override fun onCleared() {
        logger.debug("onCleared()")
        super.onCleared()
    }
}