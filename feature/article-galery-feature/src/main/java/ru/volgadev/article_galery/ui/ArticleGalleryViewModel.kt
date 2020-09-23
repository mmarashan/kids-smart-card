package ru.volgadev.article_galery.ui

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
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

    val articles: LiveData<ArrayList<Article>> = articleRepository.articles().asLiveData()

    val tracks = musicRepository.musicTracks().asLiveData()

    val audioToPlay = LiveEvent<MusicTrack>()

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