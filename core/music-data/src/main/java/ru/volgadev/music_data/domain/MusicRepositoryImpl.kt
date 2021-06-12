package ru.volgadev.music_data.domain

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import ru.volgadev.common.isValidUrlString
import ru.volgadev.common.log.Logger
import ru.volgadev.downloader.Downloader
import ru.volgadev.music_data.domain.model.MusicTrack
import ru.volgadev.music_data.domain.model.MusicTrackType
import java.io.File
import java.net.ConnectException

internal class MusicRepositoryImpl(
    private val context: Context,
    private val musicBackendApi: MusicBackendApi,
    private val musicTrackDatabase: MusicTrackDatabase,
    private val ioDispatcher: CoroutineDispatcher
) : MusicRepository {

    private val logger = Logger.get("MusicRepositoryImpl")
    private val musicTracksFlow = MutableSharedFlow<ArrayList<MusicTrack>>(replay = 1)

    private val articleAudiosFlow = MutableSharedFlow<ArrayList<MusicTrack>>(replay = 1)

    private val scope: CoroutineScope = CoroutineScope(ioDispatcher + Job())

    private val downloader = Downloader(context, scope)

    override fun musicTracks() = musicTracksFlow

    override fun articleAudios() = articleAudiosFlow

    override suspend fun loadArticleAudio(url: String): MusicTrack? = withContext(ioDispatcher) {
        logger.debug("loadArticleAudio($url)")
        val loadedTrack = loadMusicTrack(url, MusicTrackType.ARTICLE_AUDIO)
        loadedTrack?.also {
            val audios = articleAudiosFlow.first()
            var updated = false
            audios.forEach { audio ->
                if (audio.url == it.url) {
                    audio.filePath = it.url
                    updated = true
                }
            }
            if (updated) articleAudiosFlow.emit(audios)
        }
    }

    override suspend fun getTrackFromStorage(url: String): MusicTrack? = withContext(ioDispatcher) {
        logger.debug("getTrackFromStorage()")
        return@withContext musicTrackDatabase.dao().getByUrl(url)
    }

    private suspend fun loadMusicTrack(url: String, type: MusicTrackType): MusicTrack? =
        withContext(ioDispatcher) {
            logger.debug("loadMusicTrack($url)")
            if (url.isValidUrlString()) {
                val filesDir = context.filesDir
                val fileName: String = url.substring(url.lastIndexOf('/') + 1, url.length)
                val newFilePath = File(filesDir, fileName).absolutePath
                val isSuccess = downloader.download(url, newFilePath)
                if (isSuccess) {
                    logger.debug("Success load")
                    val newTrack = MusicTrack(url, newFilePath, type)
                    musicTrackDatabase.dao().insertAll(newTrack)
                    return@withContext newTrack
                } else {
                    logger.warn("Fail load")
                    return@withContext null
                }
            } else {
                logger.warn("Url not valid")
                return@withContext null
            }
        }

    init {
        logger.debug("init")
        scope.launch {
            try {
                loadFromDB()
                updateMusicTracks()
            } catch (e: ConnectException) {
                logger.error("Exception when load music tracks $e")
            }
        }
    }

    @Throws(ConnectException::class)
    private suspend fun updateMusicTracks() = withContext(ioDispatcher) {
        val fromBackendTracks = musicBackendApi.getTracks()
        val tracksToDownloading = mutableListOf<MusicTrack>()
        val inStorageTracks =
            musicTrackDatabase.dao().getAll().map { it.url to it.filePath }.toMap()
        for (track in fromBackendTracks) {
            val pathInStorage = inStorageTracks[track.url]
            if (pathInStorage != null) {
                track.filePath = pathInStorage
            } else {
                tracksToDownloading.add(track)
            }
        }
        musicTracksFlow.emit(ArrayList(fromBackendTracks))
        loadTracksToStorage(tracksToDownloading)
    }

    private suspend fun loadTracksToStorage(tracks: List<MusicTrack>) = withContext(ioDispatcher) {
        logger.debug("loadTracks(${tracks.size} tracks)")
        for (track in tracks) {
            loadMusicTrack(track.url, MusicTrackType.MUSIC)?.also { loadedTrack ->
                val audios = musicTracksFlow.first()
                var updated = false
                audios.forEach { audio ->
                    if (audio.url == loadedTrack.url) {
                        audio.filePath = loadedTrack.url
                        updated = true
                    }
                }
                if (updated) {
                    musicTracksFlow.emit(audios)
                }
            }
        }
    }

    private suspend fun loadFromDB() = withContext(ioDispatcher) {
        val musicTracks = musicTrackDatabase.dao().getAllByType(MusicTrackType.MUSIC)
        musicTracksFlow.emit(ArrayList(musicTracks))

        val articleAudios = musicTrackDatabase.dao().getAllByType(MusicTrackType.ARTICLE_AUDIO)
        articleAudiosFlow.emit(ArrayList(articleAudios))
    }
}