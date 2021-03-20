package ru.volgadev.article_data.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.volgadev.article_data.domain.ArticleCategoriesDatabase
import ru.volgadev.article_data.domain.ArticleCategory
import ru.volgadev.article_data.domain.ArticleChannelsDao

@Database(entities = [ArticleCategory::class], version = 2)
internal abstract class ArticleCategoriesDatabaseImpl : ArticleCategoriesDatabase, RoomDatabase() {

    abstract override fun dao(): ArticleChannelsDao

    companion object {
        @Volatile
        private var INSTANCE: ArticleCategoriesDatabase? = null

        private const val DATABASE_NAME = "article-categories-database.db"

        fun getInstance(context: Context): ArticleCategoriesDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ArticleCategoriesDatabaseImpl::class.java, DATABASE_NAME
            ).build()
    }
}