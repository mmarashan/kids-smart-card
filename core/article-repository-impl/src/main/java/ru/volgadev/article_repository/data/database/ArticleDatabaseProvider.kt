package ru.volgadev.article_repository.data.database

import android.content.Context
import ru.volgadev.article_repository.domain.database.ArticleDatabase

/**
 * Provider hides Room dependencies from internal module
 */
class ArticleDatabaseProvider {
    companion object {
        @JvmStatic
        fun createArticleDatabase(context: Context): ArticleDatabase =
            ArticleDatabaseImpl.getInstance(context)
    }
}