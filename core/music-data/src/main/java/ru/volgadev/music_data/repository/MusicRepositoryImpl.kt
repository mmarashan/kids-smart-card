package ru.volgadev.music_data.repository

import android.content.Context
import androidx.annotation.WorkerThread
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import ru.volgadev.music_data.api.MusicBackendApi
import ru.volgadev.music_data.storage.MusicTrackDatabase
import ru.volgadev.music_data.model.MusicTrack
import ru.volgadev.common.log.Logger
import ru.volgadev.downloader.Downloader
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

    private val storage: MusicTrackDatabase by lazy {
        MusicTrackDatabase.getInstance(context)
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
        val newTracks = musicBackendApi.getTracks()
        // TODO: логика загрузки и кэширования
        for (track in newTracks){

        }
        storage.dao().insertAll(*newTracks.toTypedArray())
        audiosChannel.offer(ArrayList(newTracks))
    }

    private suspend fun loadFromDB() = withContext(Dispatchers.Default) {
        val tracks = storage.dao().getAll()
        logger.debug("Load ${tracks.size} entities from Db")
        audiosChannel.offer(ArrayList(tracks))
    }
}