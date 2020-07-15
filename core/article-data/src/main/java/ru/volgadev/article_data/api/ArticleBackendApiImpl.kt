package ru.volgadev.article_data.api

import androidx.annotation.WorkerThread
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import ru.volgadev.common.BACKEND_URL
import ru.volgadev.common.log.Logger
import ru.volgadev.article_data.model.Article
import java.net.ConnectException


@WorkerThread
class ArticleBackendApiImpl : ArticleBackendApi {

    var client = OkHttpClient()
    private val logger = Logger.get("ArticleBackendApiImpl")

    @Throws(ConnectException::class)
    override fun getUpdates(lastUpdateTime: Long): List<Article>  {
        val request: Request = Request.Builder().apply {
            url("$BACKEND_URL/articles.json")
        }.build()

        val result = arrayListOf<Article>()

        try {
            val response: Response = client.newCall(request).execute()
            val stringResponse = response.body!!.string()
            logger.debug("stringResponse $stringResponse")
            val json = JSONObject(stringResponse)
            val articlesArray = json.getJSONArray("articles")
            for (i in 0 until articlesArray.length()) {
                val articleJson = articlesArray[i] as JSONObject
                val id = articleJson.optLong("id")
                val tags = arrayListOf<String>()
                val tagsJson = articleJson.optJSONArray("tags")
                tagsJson?.let { tagsJs ->
                    for (t in 0 until tagsJs.length()) {
                        tags.add(tagsJs[t] as String)
                    }
                }
                val title = articleJson.optString("title")
                val text = articleJson.optString("text")
                val iconUrl = articleJson.optString("iconUrl")
                val averageTimeReadingMin = articleJson.optInt("averageTimeReadingMin")
                val hardLevel = articleJson.optInt("hardLevel")
                val timestamp = articleJson.optLong("timestamp")
                result.add(
                    Article(
                        id = id, tags = tags, title = title, text = text, iconUrl = iconUrl,
                        averageTimeReadingMin = averageTimeReadingMin, hardLevel = hardLevel,
                        timestamp = timestamp
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Error when get new articles $e")
            throw ConnectException("Error when get new articles $e")
        }
        return result
    }

    override fun getTags(): List<String> {
        TODO("Not yet implemented")
    }

}