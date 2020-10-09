package ru.volgadev.article_galery.ui

import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
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

    private val _category = MutableLiveData<String>()
    val currentCategory: LiveData<String> = _category

    private val _articles = MutableLiveData<List<Article>>()
    val currentArticles: LiveData<List<Article>> = _articles

    val tracks = musicRepository.musicTracks().asLiveData()

    val audioToPlay = LiveEvent<MusicTrack>()

    val categories = articleRepository.categories().asLiveData()

    private var cachedArticles = ArrayList<Article>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            articleRepository.articles().collect { articlesList ->
                cachedArticles = articlesList
            }
        }
    }

    @MainThread
    fun onClickCategory(categoryName: String) {
        logger.debug("onClickCategory $categoryName")
        val categories = categories.value
        val categoryExists = categories?.any { category -> category.name == categoryName } ?: false
        if (categoryExists) {
            _category.value = categoryName
            val categoryArticles =
                cachedArticles.filter { article -> article.category == categoryName }
            _articles.value = categoryArticles
        } else {
            throw IllegalStateException("Categories undefined or not exists category $categoryName")
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