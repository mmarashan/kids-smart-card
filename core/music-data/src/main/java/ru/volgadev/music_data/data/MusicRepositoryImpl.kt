package ru.volgadev.music_data.data

import android.content.Context
import androidx.annotation.WorkerThread
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import ru.volgadev.common.isValidUrlString
import ru.volgadev.common.log.Logger
import ru.volgadev.downloader.Downloader
import ru.volgadev.music_data.domain.MusicBackendApi
import ru.volgadev.music_data.domain.MusicRepository
import ru.volgadev.music_data.domain.MusicTrack
import ru.volgadev.music_data.domain.MusicTrackDatabase
import ru.volgadev.music_data.domain.MusicTrackType
import java.io.File
import java.net.ConnectException

internal class MusicRepositoryImpl(
    private val context: Context,
    private val musicBackendApi: MusicBackendApi,
    private val musicTrackDatabase: MusicTrackDatabase
) : MusicRepository {

    private val logger = Logger.get("MusicRepositoryImpl")
    private val musicTracksChannel = ConflatedBroadcastChannel<ArrayList<MusicTrack>>(arrayListOf())

    // TODO: to StateFlow
    private val articleAudiosChannel =
        ConflatedBroadcastChannel<ArrayList<MusicTrack>>(arrayListOf())

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job())

    private val downloader = Downloader(context, scope)

    override fun musicTracks(): Flow<ArrayList<MusicTrack>> = musicTracksChannel.asFlow()

    override fun articleAudios(): Flow<ArrayList<MusicTrack>> = articleAudiosChannel.asFlow()

    override suspend fun loadArticleAudio(url: String): MusicTrack? = withContext(Dispatchers.IO) {
        logger.debug("loadArticleAudio($url)")
        loadMusicTrack(url, MusicTrackType.ARTICLE_AUDIO)?.also { loadedTrack ->
            val audios = articleAudiosChannel.value
            var updated = false
            audios.forEach { audio ->
                if (audio.url == loadedTrack.url) {
                    audio.filePath = loadedTrack.url
                    updated = true
                }
            }
            if (updated) {
                articleAudiosChannel.offer(audios)
            }
        }
    }

    override suspend fun getTrackFromStorage(url: String): MusicTrack? =
        withContext(Dispatchers.Default) {
            logger.debug("getTrackFromStorage()")
            return@withContext musicTrackDatabase.dao().getByUrl(url)
        }

    private suspend fun loadMusicTrack(url: String, type: MusicTrackType): MusicTrack? =
        withContext(Dispatchers.IO) {
            logger.debug("loadMusicTrack($url)")
            if (url.isValidUrlString()) {
                val filesDir = context.filesDir
                val fileName: String =
                    url.substring(url.lastIndexOf('/') + 1, url.length)
                val newFilePath = File(filesDir, fileName).absolutePath
                logger.debug("Try to load $url to $newFilePath")
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
                logger.debug("Url not valid")
                return@withContext null
            }
        }

    init {
        logger.debug("init")
        scope.launch(Dispatchers.Default) {
            loadAudios()
        }
    }

    @WorkerThread
    private suspend fun loadAudios() {
        try {
            logger.debug("Load data from DB")
            loadFromDB()
            logger.debug("Try to update data")
            updateMusicTracks()
        } catch (e: ConnectException) {
            logger.error("Exception when load music tracks $e")
        }
    }

    @Throws(ConnectException::class)
    private suspend fun updateMusicTracks() = withContext(Dispatchers.IO) {
        val fromBackendTracks = musicBackendApi.getTracks()
        val tracks = mutableListOf<MusicTrack>()
        val tracksToDownloading = mutableListOf<MusicTrack>()
        val inStorageTracks =
            musicTrackDatabase.dao().getAll().map { track -> track.url to track.filePath }.toMap()
        for (track in fromBackendTracks) {
            val urlStr = track.url
            val pathInStorage = inStorageTracks[urlStr]
            if (pathInStorage != null) {
                track.filePath = pathInStorage
            } else {
                tracksToDownloading.add(track)
            }
            tracks.add(track)
        }
        musicTracksChannel.offer(ArrayList(fromBackendTracks))
        loadMusicTracks(tracksToDownloading)
    }

    private suspend fun loadMusicTracks(newTracks: List<MusicTrack>) = withContext(Dispatchers.IO) {
        logger.debug("loadTracks(${newTracks.size} tracks)")
        for (track in newTracks) {
            loadMusicTrack(track.url, MusicTrackType.MUSIC)?.also { loadedTrack ->
                val audios = musicTracksChannel.value
                var updated = false
                audios.forEach { audio ->
                    if (audio.url == loadedTrack.url) {
                        audio.filePath = loadedTrack.url
                        updated = true
                    }
                }
                if (updated) {
                    musicTracksChannel.offer(audios)
                }
            }
        }
    }

    private suspend fun loadFromDB() = withContext(Dispatchers.Default) {
        val musicTracks = musicTrackDatabase.dao().getAllByType(MusicTrackType.MUSIC)
        logger.debug("Load ${musicTracks.size} music audio from Db")
        musicTracksChannel.offer(ArrayList(musicTracks))

        val articleAudios = musicTrackDatabase.dao().getAllByType(MusicTrackType.ARTICLE_AUDIO)
        logger.debug("Load ${articleAudios.size} article tracks from Db")
        articleAudiosChannel.offer(ArrayList(articleAudios))
    }
}