package ru.volgadev.music_data.repository

import android.content.Context
import androidx.annotation.WorkerThread
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import ru.volgadev.common.isValidUrlString
import ru.volgadev.common.log.Logger
import ru.volgadev.downloader.Downloader
import ru.volgadev.music_data.api.MusicBackendApi
import ru.volgadev.music_data.model.MusicTrack
import ru.volgadev.music_data.storage.MusicTrackDatabase
import java.io.File
import java.net.ConnectException

class MusicRepositoryImpl private constructor(
    private val context: Context,
    private val musicBackendApi: MusicBackendApi
) : MusicRepository {

    private val logger = Logger.get("MusicRepositoryImpl")
    private val audiosChannel = ConflatedBroadcastChannel<ArrayList<MusicTrack>>()

    val job = Job()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + job)

    private val downloader = Downloader(context, scope)

    private val storageDao by lazy {
        MusicTrackDatabase.getInstance(context).dao()
    }

    override fun musicTracks(): Flow<ArrayList<MusicTrack>> = audiosChannel.asFlow()

    init {
        logger.debug("init")
        CoroutineScope(Dispatchers.Default).launch {
            updateAudios()
        }
    }

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: MusicRepositoryImpl? = null

        fun getInstance(
            context: Context, musicBackendApi: MusicBackendApi
        ) =
            instance ?: synchronized(this) {
                instance ?: MusicRepositoryImpl(
                    context,
                    musicBackendApi
                ).also { instance = it }
            }
    }

    @WorkerThread
    private suspend fun updateAudios() {
        try {
            logger.debug("Try to update data")
            updateTracks()
        } catch (e: ConnectException) {
            logger.error("Exception when update article $e")
            logger.debug("Load data from DB")
            loadFromDB()
        }
    }

    @Throws(ConnectException::class)
    private suspend fun updateTracks() = withContext(Dispatchers.IO) {
        val fromBackendTracks = musicBackendApi.getTracks()
        val tracks = mutableListOf<MusicTrack>()
        val tracksToDownloading = mutableListOf<MusicTrack>()
        val inStorageTracks =
            storageDao.getAll().map { track -> track.url to track.filePath }.toMap()
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
        audiosChannel.offer(ArrayList(fromBackendTracks))
        loadTracks(tracksToDownloading)
    }
    // TODO: проверять наличие в файловой системе
    private suspend fun loadTracks(newTracks: List<MusicTrack>) = withContext(Dispatchers.IO) {
        for (track in newTracks) {
            val urlStr = track.url

            if (urlStr.isValidUrlString()) {
                val filesDir = context.filesDir
                val fileName: String =
                    urlStr.substring(urlStr.lastIndexOf('/') + 1, urlStr.length)
                val newFilePath = File(filesDir, fileName).absolutePath
                logger.debug("Try to load $urlStr to $newFilePath")
                val isSuccess = downloader.download(urlStr, newFilePath)
                if (isSuccess) {
                    logger.debug("Success load")
                    val newTrack = MusicTrack(urlStr, newFilePath)
                    storageDao.insertAll(newTrack)
                }
            } else {
                logger.debug("Url not valid")
            }
        }

    }

    private suspend fun loadFromDB() = withContext(Dispatchers.Default) {
        val tracks = storageDao.getAll()
        logger.debug("Load ${tracks.size} entities from Db")
        audiosChannel.offer(ArrayList(tracks))
    }
}