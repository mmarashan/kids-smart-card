package ru.volgadev.cardrepository.data.datasource

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.*
import ru.volgadev.cardrepository.data.datasource.dto.CardsResponseDto
import ru.volgadev.cardrepository.data.datasource.dto.CategoriesResponseDto
import ru.volgadev.cardrepository.data.datasource.mapper.Mapper
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.cardrepository.domain.model.CardCategory
import java.io.IOException
import javax.inject.Inject

internal class CardRemoteDataSourceImpl @Inject constructor(
    private val baseUrl: String,
    private val client: OkHttpClient
) : CardRemoteDataSource {

    @Throws(IOException::class)
    override suspend fun getCards(category: CardCategory): List<Card> {
        val responseDto = client.executeGet(category.fileUrl, CardsResponseDto::class.java)
            .catch { throw IOException("Bad call getCards() $it") }.firstOrNull()
            ?: throw IOException("Closed call getCards() ...")
        return Mapper.map(responseDto)
    }

    @Throws(IOException::class)
    override suspend fun getCategories(): List<CardCategory> {
        val responseDto =
            client.executeGet("$baseUrl/category.json", CategoriesResponseDto::class.java)
                .catch { throw IOException("Bad call getCategories() $it") }.firstOrNull()
                ?: throw IOException("Bad call getCategories() ...")
        return Mapper.map(responseDto)
    }

    @Throws(JsonSyntaxException::class, IOException::class, NullPointerException::class)
    private fun <T> OkHttpClient.executeGet(url: String, classOfT: Class<T>): Flow<T> =
        callbackFlow {
            val request: Request = Request.Builder().url(url).method("GET", null).build()
            val call = newCall(request).apply {
                enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        close(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val stringResponse = response.body?.string()
                            ?: throw NullPointerException("Empty request body")
                        try {
                            val value = Gson().fromJson(stringResponse, classOfT)
                            trySend(value)
                        } catch (e: JsonSyntaxException) {
                            close(e)
                        }
                    }
                })
            }

            awaitClose { call.cancel() }
        }
}