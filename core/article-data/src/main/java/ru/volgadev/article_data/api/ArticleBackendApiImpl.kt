package ru.volgadev.article_data.api

import androidx.annotation.WorkerThread
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.model.ArticlePage
import ru.volgadev.article_data.model.PageType
import ru.volgadev.common.BACKEND_URL
import ru.volgadev.common.log.Logger
import java.net.ConnectException


@WorkerThread
class ArticleBackendApiImpl : ArticleBackendApi {

    private val client by lazy { OkHttpClient() }
    private val logger = Logger.get("ArticleBackendApiImpl")

    private companion object {
        const val ARTICLES_BACKEND_URL = "$BACKEND_URL/articles.json"
        const val ARTICLE_PAGES_BACKEND_URL = "$BACKEND_URL/articles"
    }

    @Throws(ConnectException::class)
    override fun getUpdates(lastUpdateTime: Long): List<Article> {
        val request: Request = Request.Builder().apply {
            url(ARTICLES_BACKEND_URL)
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
                val author = articleJson.optString("author")
                val title = articleJson.optString("title")
                val pagesFile = articleJson.optString("pagesFile")
                val iconUrl = articleJson.optString("iconUrl")
                val averageTimeReadingMin = articleJson.optInt("averageTimeReadingMin")
                val timestamp = articleJson.optLong("timestamp")
                result.add(
                    Article(
                        id = id,
                        tags = tags,
                        author = author,
                        title = title,
                        pagesFile = pagesFile,
                        iconUrl = iconUrl,
                        averageTimeReadingMin = averageTimeReadingMin,
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

    @Throws(ConnectException::class)
    override fun getArticlePages(article: Article): List<ArticlePage> {
        val pagesFile = article.pagesFile
        val request: Request = Request.Builder().apply {
            url("$ARTICLE_PAGES_BACKEND_URL/$pagesFile")
        }.build()

        val result = arrayListOf<ArticlePage>()

        try {
            val response: Response = client.newCall(request).execute()
            val stringResponse = response.body!!.string()
            logger.debug("stringResponse $stringResponse")
            val json = JSONObject(stringResponse)
            val articlesArray = json.getJSONArray("pages")
            for (i in 0 until articlesArray.length()) {
                val articleJson = articlesArray[i] as JSONObject
                val type = articleJson.optString("type")
                val title = articleJson.optString("title")
                val text = articleJson.optString("text")
                val imageUrl = articleJson.optString("imageUrl")
                val soundUrl = articleJson.optString("soundUrl")
                val allowBackgroundSound = articleJson.optBoolean("allowBackgroundSound")
                result.add(
                    ArticlePage(
                        articleId = article.id,
                        pageNum = i,
                        type = PageType.valueOf(type),
                        title = title,
                        text = text,
                        imageUrl = imageUrl,
                        soundUrl = soundUrl,
                        allowBackgroundSound = allowBackgroundSound
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Error when get new article pages $e")
            throw ConnectException("Error when get new article pages $e")
        }
        return result
    }
}