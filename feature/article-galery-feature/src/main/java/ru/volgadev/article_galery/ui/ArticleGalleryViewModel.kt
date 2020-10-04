package ru.volgadev.article_galery.ui

import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.LiveEvent
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.model.MusicTrack
import ru.volgadev.music_data.model.MusicTrackType
import ru.volgadev.music_data.repository.MusicRepository

class ArticleGalleryViewModel(
    private val articleRepository: ArticleRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val logger = Logger.get("ArticleGalleryViewModel")

    private val articlesLiveData = articleRepository.articles().asLiveData()

    private val _category = MutableLiveData<String>()
    val currentCategory: LiveData<String> = _category

    private val _articles = MutableLiveData<List<Article>>()
    val currentArticles: LiveData<List<Article>> = _articles

    val tracks = musicRepository.musicTracks().asLiveData()

    val audioToPlay = LiveEvent<MusicTrack>()

    private val _categories = MutableLiveData<Set<String>>()
    val categories: LiveData<Set<String>> = _categories

    private var cachedArticles = ArrayList<Article>()

    // TODO: cache current category, show articles by category
    private val articlesObserver = Observer<ArrayList<Article>> { articles ->
        val categories = articles.map { article -> article.category }.toSet()
        cachedArticles = articles
        _categories.value = categories
        if (categories.isNotEmpty()) {
            val category = categories.first()
            onClickCategory(category)
        } else {
            logger.warn("Empty categories set!")
        }
    }

    init {
        articlesLiveData.observeForever(articlesObserver)
    }

    @MainThread
    fun onClickCategory(category: String) {
        logger.debug("onClickCategory $category")
        val categories = categories.value
        if (categories != null && categories.contains(category)) {
            _category.value = category
            val categoryArticles = cachedArticles.filter { article -> article.category == category }
            _articles.value = categoryArticles
        } else {
            throw IllegalStateException("Categories undefined or not exists category $category")
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
        articlesLiveData.removeObserver(articlesObserver)
        super.onCleared()
    }
}