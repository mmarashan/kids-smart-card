package ru.volgadev.article_repository.data.datasource

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.volgadev.article_repository.data.datasource.dto.ArticlesResponseDto
import ru.volgadev.article_repository.data.datasource.dto.CategoriesResponseDto
import ru.volgadev.article_repository.data.datasource.mapper.Mapper
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import java.io.IOException
import javax.inject.Inject

@WorkerThread
class ArticleRemoteDataSourceImpl @Inject constructor(
    baseUrl: String,
    private val client: OkHttpClient
) : ArticleRemoteDataSource {

    private val CATEGORIES_BACKEND_URL = "$baseUrl/category.json"

    @Throws(Exception::class)
    override fun getArticles(category: ArticleCategory): List<Article> {
        val responseDto = executeGet(category.fileUrl, ArticlesResponseDto::class.java)
        return Mapper.map(responseDto)
    }

    @Throws(Exception::class)
    override fun getCategories(): List<ArticleCategory> {
        val responseDto = executeGet(CATEGORIES_BACKEND_URL, CategoriesResponseDto::class.java)
        return Mapper.map(responseDto)
    }

    @Throws(JsonSyntaxException::class, IOException::class, NullPointerException::class)
    private fun <T> executeGet(url: String, classOfT: Class<T>): T {
        val request: Request = Request.Builder().url(url).method("GET", null).build()
        val response = client.newCall(request).execute()
        val stringResponse = response.body!!.string()
        return Gson().fromJson(stringResponse, classOfT)
    }
}