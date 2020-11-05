package ru.volgadev.article_galery.ui

import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.LiveEvent
import ru.volgadev.common.log.Logger
import ru.volgadev.common.observeOnce
import ru.volgadev.music_data.model.MusicTrack
import ru.volgadev.music_data.model.MusicTrackType
import ru.volgadev.music_data.repository.MusicRepository

class ArticleGalleryViewModel(
    private val articleRepository: ArticleRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val logger = Logger.get("ArticleGalleryViewModel")

    private val _category = MutableLiveData<ArticleCategory>()
    val currentCategory: LiveData<ArticleCategory> = _category

    private val _articles = MutableLiveData<List<Article>>()
    val currentArticles: LiveData<List<Article>> = _articles

    val tracks = musicRepository.musicTracks().asLiveData()

    val audioToPlay = LiveEvent<MusicTrack>()

    val availableCategories = articleRepository.categories().asLiveData().map { categories ->
        return@map categories.filter { category -> category.isFree || category.isPaid }
    }

    init {
        availableCategories.observeOnce { categories ->
            categories.firstOrNull()?.let { c ->
                _category.postValue(c)
            }
        }
    }

    @MainThread
    fun onClickCategory(category: ArticleCategory) {
        logger.debug("onClickCategory ${category.name}")
        _category.value = category
        viewModelScope.launch {
            val categoryArticles = articleRepository.getCategoryArticles(category)
            _articles.value = categoryArticles
        }
    }

    @MainThread
    fun onClickArticle(article: Article) {
        logger.debug("onClickArticle ${article.title}")
        article.onClickSounds.forEach { audioUrl ->
            viewModelScope.launch {
                val loadedAudio = musicRepository.getTrackFromStorage(audioUrl)
                if (loadedAudio != null) {
                    logger.debug("Audio already loaded")
                    audioToPlay.postValue(loadedAudio)
                } else {
                    audioToPlay.postValue(MusicTrack(audioUrl, null, MusicTrackType.ARTICLE_AUDIO))
                    logger.debug("Load sound..")
                    val track = musicRepository.loadArticleAudio(audioUrl)
                    logger.debug("Success loaded: ${track != null}")
                }
            }
        }
    }

    override fun onCleared() {
        logger.debug("onCleared()")
        super.onCleared()
    }
}