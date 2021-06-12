package ru.volgadev.article_galery.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import ru.volgadev.article_repository.domain.ArticleRepository
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.core.musicplayer.api.MusicPlayer
import ru.volgadev.core.musicplayer.api.PlayerTrack
import ru.volgadev.music_data.domain.MusicRepository
import ru.volgadev.music_data.domain.model.MusicTrack
import ru.volgadev.music_data.domain.model.MusicTrackType
import java.io.File
import java.net.URI

internal interface ArticleGalleryInteractor {
    fun availableCategories(): Flow<List<ArticleCategory>>

    suspend fun getCategoryArticles(category: ArticleCategory): List<Article>

    suspend fun startBackgroundPlayer()

    suspend fun pauseBackgroundPlayer()

    suspend fun playCardSounds(article: Article)
}

internal class ArticleGalleryInteractorImpl(
    private val articleRepository: ArticleRepository,
    private val musicRepository: MusicRepository,
    private val musicPlayer: MusicPlayer,
    private val cardPlayer: MusicPlayer,
    private val isBackgroundMusicEnabled: Boolean
) : ArticleGalleryInteractor {

    private var musicTracks: List<PlayerTrack>? = null

    override fun availableCategories(): Flow<List<ArticleCategory>> =
        articleRepository.categories.map { categories ->
            categories.filter { (it.isFree || it.isPaid) }
        }

    override suspend fun getCategoryArticles(category: ArticleCategory) =
        articleRepository.getCategoryArticles(category)

    override suspend fun startBackgroundPlayer() {
        if (isBackgroundMusicEnabled) {
            if (musicTracks == null) {
                loadMusicTracks()
                val tracks = musicTracks
                if (!tracks.isNullOrEmpty()) musicPlayer.setPlaylist(tracks)
            }

            musicPlayer.play()
        }
    }

    override suspend fun pauseBackgroundPlayer() {
        if (isBackgroundMusicEnabled) {
            musicPlayer.pause()
        }
    }

    override suspend fun playCardSounds(article: Article) {
        article.onClickSounds.forEach { audioUrl ->
            val track = getCardTrack(audioUrl)
            track?.let {
                cardPlayer.setPlaylist(listOf(track))
                cardPlayer.play()
            }
        }
    }

    private suspend fun loadMusicTracks() {
        val tracks = musicRepository.musicTracks().firstOrNull().orEmpty()
        musicTracks = tracks.mapNotNull { map(it) }
    }

    private fun map(musicTrack: MusicTrack): PlayerTrack? {
        return try {
            val filePath = musicTrack.filePath
            PlayerTrack(
                id = musicTrack.url,
                remoteUri = URI(musicTrack.url),
                file = if (filePath != null) File(filePath) else null
            )
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getCardTrack(url: String): PlayerTrack? {
        val loadedAudio = musicRepository.getTrackFromStorage(url)
        return if (loadedAudio != null) {
            map(loadedAudio)
        } else {
            musicRepository.loadArticleAudio(url)
            map(MusicTrack(url = url, filePath = null, type = MusicTrackType.ARTICLE_AUDIO))
        }
    }
}

