package ru.volgadev.article_repository.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.volgadev.article_repository.domain.database.ArticleCategoriesDatabase
import ru.volgadev.article_repository.domain.model.ArticleCategory

@Database(entities = [ArticleCategory::class], version = 2)
internal abstract class ArticleCategoriesDatabaseImpl : ArticleCategoriesDatabaseInterface, RoomDatabase() {

    abstract override fun dao(): ArticleCategoriesDaoImpl

    companion object {
        @Volatile
        private var INSTANCE: ArticleCategoriesDatabaseInterface? = null

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