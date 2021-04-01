package ru.volgadev.article_galery.presentation

import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import ru.volgadev.article_repository.domain.ArticleRepository
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.common.FeatureToggles
import ru.volgadev.common.LiveEvent
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.domain.MusicRepository
import ru.volgadev.music_data.domain.model.MusicTrack
import ru.volgadev.music_data.domain.model.MusicTrackType

@OptIn(InternalCoroutinesApi::class)
class ArticleGalleryViewModel(
    private val articleRepository: ArticleRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val logger = Logger.get("ArticleGalleryViewModel")

    private val _category = MutableLiveData<ArticleCategory>()
    val currentCategory: LiveData<ArticleCategory> = _category.distinctUntilChanged()

    private val _articles = MutableLiveData<List<Article>>()
    val currentArticles: LiveData<List<Article>> = _articles

    val tracks = if (FeatureToggles.ENABLE_BACKGROUND_MUSIC) {
        musicRepository.musicTracks().asLiveData()
    } else {
        MutableLiveData<List<MusicTrack>>()
    }

    val audioToPlay = LiveEvent<MusicTrack>()

    private val _categories = MutableLiveData<List<ArticleCategory>>()
    val availableCategories = _categories

    init {
        logger.debug("init")
        viewModelScope.launch(Dispatchers.Default) {
            articleRepository.categories().collect(object : FlowCollector<List<ArticleCategory>> {
                override suspend fun emit(value: List<ArticleCategory>) {
                    logger.debug("On update categories")
                    val filteredCategories = value.filter { category ->
                        logger.debug("Filter category ${category.name} isPaid = ${category.isPaid} isFree=${category.isFree}")
                        (category.isFree || category.isPaid)
                    }
                    _categories.postValue(filteredCategories)
                }
            })
        }
    }

    @MainThread
    fun onClickCategory(category: ArticleCategory) {
        logger.debug("onClickCategory ${category.name}")
        if (_category.value?.id != category.id) {
            _category.value = category
            viewModelScope.launch {
                val categoryArticles = articleRepository.getCategoryArticles(category)
                _articles.value = categoryArticles
            }
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
}