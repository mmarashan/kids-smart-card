package ru.volgadev.article_page

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.volgadev.article_data.domain.ArticlePage
import ru.volgadev.article_data.domain.ArticleRepository
import ru.volgadev.common.ErrorResult
import ru.volgadev.common.SuccessResult
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.domain.MusicRepository

class ReadingState(
    var page: ArticlePage,
    var pageNum: Int,
    var pagesCount: Int
)

class ArticlePageViewModel(
    private val articleRepository: ArticleRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val logger = Logger.get("ArticlePageViewModel")

    private val _state = MutableLiveData<ReadingState>()
    val state: LiveData<ReadingState> = _state

    @Volatile
    private var pages: List<ArticlePage> = listOf()

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
                        pages = pagesResult.data
                        if (pages.isNotEmpty()) {
                            val firstPage = pages[0]
                            val readingState = ReadingState(firstPage, 0, pages.size)
                            _state.postValue(readingState)
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

    @MainThread
    fun onClickNext() {
        logger.debug("onClickNext()")
        val state = _state.value
        val currentPosition = state?.pageNum ?: Int.MAX_VALUE
        if (state != null && pages.isNotEmpty() && pages.size > currentPosition) {
            val newPosition = currentPosition + 1
            val page = pages[newPosition]
            val readingState = ReadingState(page, newPosition, pages.size)
            _state.postValue(readingState)
        }
    }

    @MainThread
    fun onClickPrev() {
        logger.debug("onClickPrev()")
        val state = _state.value
        val currentPosition = state?.pageNum ?: 0
        if (state != null && pages.isNotEmpty() && currentPosition > 0) {
            val newPosition = currentPosition - 1
            val page = pages[newPosition]
            val readingState = ReadingState(page, newPosition, pages.size)
            _state.postValue(readingState)
        }
    }
}