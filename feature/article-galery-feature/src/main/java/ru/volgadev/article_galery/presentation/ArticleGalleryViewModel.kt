package ru.volgadev.article_galery.presentation

import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import ru.volgadev.article_galery.domain.ArticleGalleryInteractor
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.common.LiveEvent
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.domain.model.MusicTrack
import ru.volgadev.music_data.domain.model.MusicTrackType

@OptIn(InternalCoroutinesApi::class)
internal class ArticleGalleryViewModel(
    private val interactor: ArticleGalleryInteractor
) : ViewModel() {

    private val logger = Logger.get("ArticleGalleryViewModel")

    private val _category = MutableLiveData<ArticleCategory>()
    val currentCategory: LiveData<ArticleCategory> = _category.distinctUntilChanged()

    private val _articles = MutableLiveData<List<Article>>()
    val currentArticles: LiveData<List<Article>> = _articles

    val tracks = interactor.musicTracks().asLiveData()

    val trackToPlaying = LiveEvent<MusicTrack>()

    private val _categories = MutableLiveData<List<ArticleCategory>>()
    val availableCategories = _categories

    init {
        logger.debug("init")
        viewModelScope.launch(Dispatchers.Default) {
            interactor.categories().collect(object : FlowCollector<List<ArticleCategory>> {
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
                val categoryArticles = interactor.getCategoryArticles(category)
                _articles.value = categoryArticles
            }
        }
    }

    @MainThread
    fun onClickArticle(article: Article) {
        logger.debug("onClickArticle ${article.title}")
        article.onClickSounds.forEach { audioUrl ->
            viewModelScope.launch {
                val loadedAudio = interactor.getTrackFromStorage(audioUrl)
                if (loadedAudio != null) {
                    trackToPlaying.postValue(loadedAudio)
                } else {
                    trackToPlaying.postValue(
                        MusicTrack(audioUrl, filePath = null, type = MusicTrackType.ARTICLE_AUDIO)
                    )
                    interactor.loadArticleAudio(audioUrl)
                }
            }
        }
    }
}