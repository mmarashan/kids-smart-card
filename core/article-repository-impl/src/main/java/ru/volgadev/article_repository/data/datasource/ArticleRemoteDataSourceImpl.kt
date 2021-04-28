package ru.volgadev.article_repository.data.datasource

import androidx.annotation.WorkerThread
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import ru.volgadev.article_repository.domain.datasource.ArticleRemoteDataSource
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.common.log.Logger
import java.net.ConnectException
import javax.inject.Inject

@WorkerThread
class ArticleRemoteDataSourceImpl @Inject constructor(
    private val baseUrl: String,
    private val client: OkHttpClient
) : ArticleRemoteDataSource {

    private val logger = Logger.get("ArticleBackendApiImpl")

    private val CATEGORIES_BACKEND_URL = "$baseUrl/category.json"

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
                val categoryId = articleJson.optString("categoryId")
                val iconUrl = articleJson.optString("iconUrl")
                val openPhrase =
                    if (!articleJson.isNull("openPhrase")) articleJson.optString("openPhrase") else null
                result.add(
                    Article(
                        id = id,
                        tags = tags,
                        author = author,
                        title = title,
                        categoryId = categoryId,
                        iconUrl = iconUrl,
                        onClickSounds = onClickSounds,
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