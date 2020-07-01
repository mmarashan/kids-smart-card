package ru.volgadev.samplefeature.data.repository

import android.content.Context
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import ru.volgadev.samplefeature.data.model.Article
import ru.volgadev.common.log.Logger
import ru.volgadev.samplefeature.data.repository.SampleRepository

class SampleRepositoryImpl(context: Context) : SampleRepository {

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

}