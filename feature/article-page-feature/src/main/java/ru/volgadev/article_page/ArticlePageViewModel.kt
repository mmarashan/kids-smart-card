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
    val isMute: LiveData<Boolean> = _mute

    private val _autoScroll = MutableLiveData<Boolean>().apply { value = true }
    val isAutoScroll: LiveData<Boolean> = _autoScroll.distinctUntilChanged()

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
    fun onClickToggleAutoScroll() {
        logger.debug("onClickAutoScroll()")
        _autoScroll.value = !_autoScroll.value!!
    }

    @MainThread
    fun onClickText(){
        _autoScroll.value = false
    }
}