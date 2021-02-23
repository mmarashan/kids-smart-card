package ru.volgadev.article_data.data

import android.content.Context
import ru.volgadev.article_data.domain.ArticleCategoriesDatabase
import ru.volgadev.article_data.domain.ArticleDatabase
import ru.volgadev.article_data.storage.ArticleCategoriesDatabaseImpl

/**
 * Provider hides Room dependencies from internal module
 */
class ArticleDatabaseProvider {
    companion object {
        fun createArticleDatabase(context: Context): ArticleDatabase = ArticleDatabaseImpl.getInstance(context)
        fun createArticleCategoriesDatabase(context: Context): ArticleCategoriesDatabase =
            ArticleCategoriesDatabaseImpl.getInstance(context)
    }
}