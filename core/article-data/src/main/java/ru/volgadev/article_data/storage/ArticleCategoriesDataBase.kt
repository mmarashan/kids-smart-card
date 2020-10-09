package ru.volgadev.article_data.storage

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.*
import ru.volgadev.article_data.model.ArticleCategory

@Dao
@WorkerThread
interface ArticleChannelsDao {
    @Query("SELECT * FROM articlecategory")
    fun getAll(): List<ArticleCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: ArticleCategory)

    @Delete
    fun delete(user: ArticleCategory)
}

@Database(entities = [ArticleCategory::class], version = 1)
abstract class ArticleCategoriesDatabase : RoomDatabase() {

    abstract fun dao(): ArticleChannelsDao

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
                ArticleCategoriesDatabase::class.java, DATABASE_NAME
            ).build()
    }
}