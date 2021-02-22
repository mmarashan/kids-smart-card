package ru.volgadev.article_data.domain

import ru.volgadev.article_data.storage.ArticleChannelsDao
import ru.volgadev.article_data.storage.ArticleDao

internal interface ArticleDatabase {
    fun dao(): ArticleDao
}

internal interface ArticleCategoriesDatabase {
    fun dao(): ArticleChannelsDao
}