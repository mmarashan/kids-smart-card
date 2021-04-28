package ru.volgadev.article_repository.data.datasource

import junit.framework.Assert.assertEquals
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class ArticleRemoteDataSourceImplTest {


    private var server = MockWebServer()
    private val client = OkHttpClient()
    private lateinit var remoteDataSource: ArticleRemoteDataSourceImpl

    @Before
    fun setup() {
        server.start(8080)
        val baseUrl: HttpUrl = server.url("")
        remoteDataSource =
            ArticleRemoteDataSourceImpl(baseUrl = baseUrl.toUri().toString(), client = client)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun a() {
        val responseBody = readFileWithNewLineFromResources("response/categoriesResponse.json")
        val response = MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(responseBody.toString())

        server.enqueue(response)

        val categories = remoteDataSource.getCategories()


        assertEquals(0, categories.size)
    }

    fun readFileWithNewLineFromResources(fileName: String): String {
        return getInputStreamFromResource(fileName)?.bufferedReader()
            .use { bufferReader -> bufferReader?.readText() } ?: ""
    }

    private fun getInputStreamFromResource(fileName: String) =
        javaClass.classLoader?.getResourceAsStream(fileName)


}