package ru.volgadev.article_repository.data.database

import android.content.Context
import ru.volgadev.article_repository.domain.database.ArticleCategoriesDatabase
import ru.volgadev.article_repository.domain.database.ArticleDatabase

/**
 * Provider hides Room dependencies from internal module
 */
class ArticleDatabaseProvider {
    companion object {

        fun createArticleDatabase(context: Context): ArticleDatabase =
            ArticleDatabaseImpl.getInstance(context)

        fun createArticleCategoriesDatabase(context: Context): ArticleCategoriesDatabase =
            ArticleCategoriesDatabaseImpl.getInstance(context)
    }
}