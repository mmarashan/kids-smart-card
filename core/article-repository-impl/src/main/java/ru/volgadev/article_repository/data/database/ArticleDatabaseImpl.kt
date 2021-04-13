package ru.volgadev.article_repository.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.volgadev.article_repository.domain.database.ArticleDatabase
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory

@Database(entities = [Article::class, ArticleCategory::class], version = 3)
@TypeConverters(ListStringConverter::class)
internal abstract class ArticleDatabaseImpl : ArticleDatabaseInterface, RoomDatabase() {

    abstract override fun dao(): ArticleDaoImpl

    companion object {
        @Volatile
        private var INSTANCE: ArticleDatabaseInterface? = null

        private const val DATABASE_NAME = "database.db"

        fun getInstance(context: Context): ArticleDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ArticleDatabaseImpl::class.java, DATABASE_NAME
            ).fallbackToDestructiveMigration().build()
    }
}