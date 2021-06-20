package ru.volgadev.cardgallery.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import ru.volgadev.cardrepository.domain.CardRepository
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.cardrepository.domain.model.CardCategory
import ru.volgadev.core.musicplayer.api.MusicPlayer
import ru.volgadev.core.musicplayer.api.PlayerTrack
import ru.volgadev.music_data.domain.MusicRepository
import ru.volgadev.music_data.domain.model.MusicTrack
import ru.volgadev.music_data.domain.model.MusicTrackType
import java.io.File
import java.net.URI

internal interface ArticleGalleryInteractor {
    fun availableCategories(): Flow<List<CardCategory>>

    suspend fun getCategoryArticles(category: CardCategory): List<Card>

    suspend fun startBackgroundPlayer()

    suspend fun pauseBackgroundPlayer()

    suspend fun nextTrack()

    suspend fun previousTrack()

    suspend fun playCardSounds(card: Card)
}

internal class ArticleGalleryInteractorImpl(
    private val cardRepository: CardRepository,
    private val musicRepository: MusicRepository,
    private val musicPlayer: MusicPlayer,
    private val cardPlayer: MusicPlayer,
    private val isBackgroundMusicEnabled: Boolean
) : ArticleGalleryInteractor {

    private var musicTracks: List<PlayerTrack>? = null

    override fun availableCategories(): Flow<List<CardCategory>> =
        cardRepository.categories.map { categories ->
            categories.filter { (it.isFree || it.isPaid) }
        }

    override suspend fun getCategoryArticles(category: CardCategory) =
        cardRepository.getCategoryCards(category)

    override suspend fun startBackgroundPlayer() {
        if (isBackgroundMusicEnabled) {
            if (musicTracks == null) {
                loadMusicTracks()
                val tracks = musicTracks?.shuffled()
                if (!tracks.isNullOrEmpty()) musicPlayer.setPlaylist(tracks)
                musicPlayer.setRepeat(repeatAll = true)
            }

            musicPlayer.play()
        }
    }

    override suspend fun pauseBackgroundPlayer() {
        if (isBackgroundMusicEnabled) musicPlayer.pause()
    }

    override suspend fun nextTrack() {
        if (isBackgroundMusicEnabled) musicPlayer.next()
    }

    override suspend fun previousTrack() {
        if (isBackgroundMusicEnabled) musicPlayer.previous()
    }

    override suspend fun playCardSounds(card: Card) {
        card.onClickSounds.forEach { audioUrl ->
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
            musicRepository.loadAudio(url)
            map(MusicTrack(url = url, filePath = null, type = MusicTrackType.ARTICLE_AUDIO))
        }
    }
}

