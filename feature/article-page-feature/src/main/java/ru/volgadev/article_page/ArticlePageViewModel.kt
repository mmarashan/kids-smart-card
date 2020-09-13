package ru.volgadev.article_page

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.volgadev.article_data.model.ArticlePage
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.ErrorResult
import ru.volgadev.common.SuccessResult
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.repository.MusicRepository

class ArticlePageViewModel(
    private val articleRepository: ArticleRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val logger = Logger.get("ArticlePageViewModel")

    private val _articlePage = MutableLiveData<ArticlePage>()
    val articlePage: LiveData<ArticlePage> = _articlePage

    private val _mute = MutableLiveData<Boolean>().apply { value = false }
    val isMute: LiveData<Boolean> = _mute.distinctUntilChanged()

    private val _progressPercent = MutableLiveData<Float>()
    val progressPercent: LiveData<Float> = _progressPercent.distinctUntilChanged()

    val tracks = musicRepository.musicTracks().asLiveData()

    @AnyThread
    fun onChooseArticle(id: Long) {
        viewModelScope.launch {
            val article = articleRepository.getArticle(id)
            if (article != null) {
                logger.debug("Use article ${article.id}")
                val pagesResult = articleRepository.getArticlePages(article)
                when (pagesResult) {
                    is SuccessResult<List<ArticlePage>> -> {
                        val pages = pagesResult.data
                        if (pages.isNotEmpty()) {
                            val firstPage = pages[0]
                            _articlePage.postValue(firstPage)
                        } else {
                            logger.warn("No pages in article $id")
                        }
                    }
                    is ErrorResult -> {
                        logger.error("Error when load pages ${pagesResult.exception.message}")
                    }
                }
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
}