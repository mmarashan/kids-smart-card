package ru.volgadev.article_data.storage

import android.content.Context

class LocalStorageProvider {
    companion object {
        fun getArticleCategoriesDatabase(context: Context): ArticleCategoriesDatabase =
            ArticleCategoriesDatabaseImpl.getInstance(context = context)

        fun getArticleDatabase(context: Context): ArticleDatabase =
            ArticleDatabaseImpl.getInstance(context = context)
    }
}