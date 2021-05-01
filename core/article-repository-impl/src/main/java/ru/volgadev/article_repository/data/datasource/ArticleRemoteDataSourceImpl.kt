package ru.volgadev.article_repository.data.datasource

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.volgadev.article_repository.data.datasource.dto.ArticlesResponseDto
import ru.volgadev.article_repository.data.datasource.dto.CategoriesResponseDto
import ru.volgadev.article_repository.data.datasource.mapper.Mapper
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.common.log.Logger
import java.net.ConnectException
import javax.inject.Inject


// TODO: refactor code doubling
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

        try {
            val response: Response = client.newCall(request).execute()
            val stringResponse = response.body!!.string()
            val dto = Gson().fromJson(stringResponse, ArticlesResponseDto::class.java)
            return Mapper.map(dto)

        } catch (e: Exception) {
            throw ConnectException("Error when get new articles ${e}")
        }
    }

    @Throws(ConnectException::class)
    override fun getCategories(): List<ArticleCategory> {
        val request: Request = Request.Builder().apply {
            url(CATEGORIES_BACKEND_URL)
        }.build()

        try {
            val response: Response = client.newCall(request).execute()
            val stringResponse = response.body!!.string()
            val dto = Gson().fromJson(stringResponse, CategoriesResponseDto::class.java)
            return Mapper.map(dto)

        } catch (e: Exception) {
            throw ConnectException("Error when get new categories $e")
        }
    }
}