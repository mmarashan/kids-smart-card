package ru.volgadev.article_galery.ui

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.repository.MusicRepository

class ArticleGalleryViewModel(private val articleRepository: ArticleRepository,
                              private val musicRepository: MusicRepository) : ViewModel() {

    private val logger = Logger.get("ArticleGalleryViewModel")

    val articles: LiveData<ArrayList<Article>> = articleRepository.articles().asLiveData()

    val tracks = musicRepository.musicTracks().asLiveData()

    val articleAudios = musicRepository.articleAudios().asLiveData()

    @MainThread
    fun onClickArticle(article: Article){
        logger.debug("onClickArticle ${article.title}")
        article.onClickSounds.forEach { audioUrl ->
            val loadedAudio = articleAudios.value?.filter { audio -> audio.url == audioUrl }
            if (loadedAudio != null) {
                logger.debug("Audio already loaded")
            } else {
                logger.debug("Load sound..")
                viewModelScope.launch(Dispatchers.IO) {
                    val track = musicRepository.loadArticleAudio(audioUrl)
                    logger.debug("Success loaded: ${track!=null}")
                }
            }
        }
    }
}