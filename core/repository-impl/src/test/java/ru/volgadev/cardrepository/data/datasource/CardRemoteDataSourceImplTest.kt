package ru.volgadev.cardrepository.data.datasource

import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.volgadev.cardrepository.domain.model.CardCategory

/**
 * Test on [CardRemoteDataSourceImpl]
 */
class CardRemoteDataSourceImplTest {

    private var server = MockWebServer()
    private val client = OkHttpClient()
    private lateinit var remoteDataSource: CardRemoteDataSourceImpl

    @Before
    fun setup() {
        server.start(8888)
        val baseUrl: HttpUrl = server.url("")
        remoteDataSource =
            CardRemoteDataSourceImpl(baseUrl = baseUrl.toUri().toString(), client = client)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun checkCategoriesParsing() = runBlocking {
        val responseBody = readFileFromResources("response/categoriesResponse.json")
        val response = MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(responseBody)

        server.enqueue(response)

        val categories = remoteDataSource.getCategories()

        assertEquals(3, categories.size)
    }

    @Test
    fun checkArticlesParsing() = runBlocking {
        val responseBody = readFileFromResources("response/articlesResponse.json")
        val response = MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(responseBody)

        server.enqueue(response)

        val someCategory = CardCategory(
            id = "",
            name = "",
            description = "",
            iconUrl = "",
            fileUrl = server.url("").toUri().toString(),
            marketItemId = "",
            isPaid = true
        )

        val articles = remoteDataSource.getCards(someCategory)

        assertEquals(3, articles.size)
    }

    private fun readFileFromResources(fileName: String): String {
        return getInputStreamFromResource(fileName)?.bufferedReader()
            .use { bufferReader -> bufferReader?.readText() } ?: ""
    }

    private fun getInputStreamFromResource(fileName: String) =
        javaClass.classLoader?.getResourceAsStream(fileName)
}