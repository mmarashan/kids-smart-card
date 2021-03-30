package ru.volgadev.article_repository.data.datasource

import androidx.annotation.WorkerThread
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import ru.volgadev.article_repository.domain.datasource.ArticleBackendApi
import ru.volgadev.article_repository.domain.model.*
import ru.volgadev.common.BACKEND_URL
import ru.volgadev.common.log.Logger
import java.net.ConnectException
import javax.inject.Inject

@WorkerThread
class ArticleBackendApiImpl @Inject constructor() : ArticleBackendApi {

    private val client by lazy { OkHttpClient() }
    private val logger = Logger.get("ArticleBackendApiImpl")

    private companion object {
        const val ARTICLE_PAGES_BACKEND_URL = "$BACKEND_URL/articles"
        const val CATEGORIES_BACKEND_URL = "$BACKEND_URL/category.json"
    }

    @Throws(ConnectException::class)
    override fun getArticles(category: ArticleCategory): List<Article> {
        logger.debug("getArticles(${category.fileUrl})")
        val request: Request = Request.Builder().apply {
            url(category.fileUrl)
        }.build()

        val result = arrayListOf<Article>()

        try {
            val response: Response = client.newCall(request).execute()
            val stringResponse = response.body!!.string()
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
                val onClickSounds = arrayListOf<String>()
                val onClickSoundsJson = articleJson.optJSONArray("onClickSounds")
                onClickSoundsJson?.let { onClickSoundsJs ->
                    for (t in 0 until onClickSoundsJs.length()) {
                        onClickSounds.add(onClickSoundsJs[t] as String)
                    }
                }
                val author = articleJson.optString("author")
                val title = articleJson.optString("title")
                val type = articleJson.optString("type")
                val categoryId = articleJson.optString("categoryId")
                val pagesFile = articleJson.optString("pagesFile")
                val iconUrl = articleJson.optString("iconUrl")
                val averageTimeReadingMin = articleJson.optInt("averageTimeReadingMin")
                val timestamp = articleJson.optLong("timestamp")
                val openPhrase =
                    if (!articleJson.isNull("openPhrase")) articleJson.optString("openPhrase") else null
                result.add(
                    Article(
                        id = id,
                        tags = tags,
                        author = author,
                        title = title,
                        categoryId = categoryId,
                        type = ArticleType.valueOf(type),
                        pagesFile = pagesFile,
                        iconUrl = iconUrl,
                        onClickSounds = onClickSounds,
                        averageTimeReadingMin = averageTimeReadingMin,
                        timestamp = timestamp,
                        openPhrase = openPhrase
                    )
                )
            }
        } catch (e: Exception) {
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
            throw ConnectException("Error when get new article pages $e")
        }
        return result
    }

    @Throws(ConnectException::class)
    override fun getCategories(): List<ArticleCategory> {
        val request: Request = Request.Builder().apply {
            url(CATEGORIES_BACKEND_URL)
        }.build()

        val result = arrayListOf<ArticleCategory>()

        try {
            val response: Response = client.newCall(request).execute()
            val stringResponse = response.body!!.string()
            val json = JSONObject(stringResponse)
            val categoriesArray = json.getJSONArray("categories")
            for (i in 0 until categoriesArray.length()) {
                val categoriesJson = categoriesArray[i] as JSONObject
                val id = categoriesJson.optString("id")
                val name = categoriesJson.optString("name")
                val description = categoriesJson.optString("description")
                val iconUrl = categoriesJson.optString("iconUrl")
                val fileUrl = categoriesJson.optString("fileUrl")
                val marketItemId =
                    if (!categoriesJson.isNull("marketItemId")) categoriesJson.optString("marketItemId") else null
                result.add(
                    ArticleCategory(
                        id = id,
                        name = name,
                        description = description,
                        iconUrl = iconUrl,
                        marketItemId = marketItemId,
                        fileUrl = fileUrl
                    )
                )
            }
        } catch (e: Exception) {
            throw ConnectException("Error when get new categories $e")
        }
        return result
    }
}