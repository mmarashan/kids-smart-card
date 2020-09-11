package ru.volgadev.article_data.api

import androidx.annotation.WorkerThread
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.model.ArticlePage

@WorkerThread
interface ArticleBackendApi {
    fun getUpdates(lastUpdateTime: Long): List<Article>
    fun getArticlePages(article: Article): List<ArticlePage>
}