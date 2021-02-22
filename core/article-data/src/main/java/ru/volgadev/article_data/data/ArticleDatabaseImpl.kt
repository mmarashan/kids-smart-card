package ru.volgadev.article_data.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.volgadev.article_data.domain.Article
import ru.volgadev.article_data.domain.ArticleDao
import ru.volgadev.article_data.domain.ArticleDatabase
import ru.volgadev.article_data.storage.ArticleTypeConverter
import ru.volgadev.article_data.storage.ListStringConverter

@Database(entities = [Article::class], version = 3)
@TypeConverters(ListStringConverter::class, ArticleTypeConverter::class)
internal abstract class ArticleDatabaseImpl : ArticleDatabase, RoomDatabase() {

    abstract override fun dao(): ArticleDao

    companion object {
        @Volatile
        private var INSTANCE: ArticleDatabase? = null

        private const val DATABASE_NAME = "article-database.db"

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