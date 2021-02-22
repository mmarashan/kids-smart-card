package ru.volgadev.article_data.domain

import androidx.annotation.WorkerThread

@WorkerThread
internal interface ArticleBackendApi {
    fun getCategories(): List<ArticleCategory>
    fun getArticles(category: ArticleCategory): List<Article>
    fun getArticlePages(article: Article): List<ArticlePage>
}