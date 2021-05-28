package ru.volgadev.article_repository.data.database

import android.content.Context

/**
 * Provider hides Room dependencies from internal module
 */
internal class ArticleDatabaseProvider {
    companion object {
        @JvmStatic
        fun createArticleDatabase(context: Context): ArticleDatabase =
            ArticleDatabaseImpl.getInstance(context)
    }
}