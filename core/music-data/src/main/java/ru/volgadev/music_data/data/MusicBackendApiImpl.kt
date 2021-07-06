package ru.volgadev.music_data.data

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.domain.MusicBackendApi
import ru.volgadev.music_data.domain.model.MusicTrack
import ru.volgadev.music_data.domain.model.MusicTrackType
import java.net.ConnectException

internal class MusicBackendApiImpl(
    private val baseUrl: String,
    private val client: OkHttpClient
) : MusicBackendApi {

    private val logger = Logger.get("MusicBackendApiImpl")

    @Throws(ConnectException::class)
    override fun getTracks(): List<MusicTrack> {
        val request: Request = Request.Builder().apply {
            url("$baseUrl/audio.json")
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
                    MusicTrack(url = url, filePath = null, type = MusicTrackType.MUSIC)
                )
            }
        } catch (e: Exception) {
            logger.error("Error when get new audio $e")
            throw ConnectException("Error when get new audio $e")
        }
        return result
    }
}