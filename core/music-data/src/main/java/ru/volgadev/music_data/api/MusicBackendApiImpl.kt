package ru.volgadev.music_data.api

import androidx.annotation.WorkerThread
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import ru.volgadev.common.BACKEND_URL
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.model.MusicTrack
import java.net.ConnectException


@WorkerThread
class MusicBackendApiImpl : MusicBackendApi {

    var client = OkHttpClient()
    private val logger = Logger.get("MusicBackendApiImpl")

    @Throws(ConnectException::class)
    override fun getTracks(): List<MusicTrack>  {
        val request: Request = Request.Builder().apply {
            url("$BACKEND_URL/audio.json")
        }.build()

        val result = arrayListOf<MusicTrack>()

        try {
            val response: Response = client.newCall(request).execute()
            val stringResponse = response.body!!.string()
            logger.debug("stringResponse $stringResponse")
            val json = JSONObject(stringResponse)
            val articlesArray = json.getJSONArray("audio")
            for (i in 0 until articlesArray.length()) {
                val url = articlesArray[i] as String
                result.add(
                    MusicTrack(
                        url = url, filePath = null
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Error when get new audio $e")
            throw ConnectException("Error when get new audio $e")
        }
        return result
    }
}