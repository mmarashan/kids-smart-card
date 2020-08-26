package ru.volgadev.article_page

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.log.Logger

class ArticlePageViewModel(private val articleRepository: ArticleRepository) : ViewModel() {

    private val logger = Logger.get("ArticlePageViewModel")

    private val _article = MutableLiveData<Article>()
    val article: LiveData<Article> = _article

    private val _mute = MutableLiveData<Boolean>().apply { value = false }
    val isMute: LiveData<Boolean> = _mute.distinctUntilChanged()

    private val _autoScroll = MutableLiveData<Boolean>().apply { value = true }
    val isAutoScroll: LiveData<Boolean> = _autoScroll.distinctUntilChanged()

    private val _progressPercent = MutableLiveData<Float>()
    val progressPercent: LiveData<Float> = _progressPercent.distinctUntilChanged()

    @AnyThread
    fun onChooseArticle(id: Long) {
        viewModelScope.launch {
            val article = articleRepository.getArticle(id)
            if (article != null) {
                logger.debug("Use article ${article.id}")
                _article.postValue(article)
            } else {
                logger.error("Article $id not found")
            }
        }
    }

    @MainThread
    fun onClickToggleMute() {
        logger.debug("onClickMute()")
        _mute.value = !_mute.value!!
    }

    @MainThread
    fun onToggleAutoScroll(value: Boolean = !_autoScroll.value!!) {
        logger.debug("onClickAutoScroll(value=$value)")
        _autoScroll.value = value
    }

    @AnyThread
    fun onScrollProgress(progressPercent: Float) {
        _progressPercent.postValue(progressPercent)
    }
}