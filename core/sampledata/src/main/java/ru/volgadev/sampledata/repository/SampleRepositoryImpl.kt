package ru.volgadev.sampledata.repository

import android.content.Context
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import ru.volgadev.common.log.Logger
import ru.volgadev.sampledata.model.Article

class SampleRepositoryImpl private constructor(context: Context) : SampleRepository {

    private val logger = Logger.get("SampleRepositoryImpl")
    private val articleChannel = Channel<ArrayList<Article>>(Channel.CONFLATED)

    override fun articles(): Flow<ArrayList<Article>> = articleChannel.consumeAsFlow()

    init {
        logger.debug("Init")

        val articles = ArrayList<Article>()

        for (i in 0..10) {
            articles.add(Article(i, "Test title", "Test text"))
        }

        articleChannel.offer(articles)
    }

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: SampleRepositoryImpl? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: SampleRepositoryImpl(context).also { instance = it }
            }
    }

}