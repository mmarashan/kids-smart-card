package ru.volgadev.article_data.db

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.*
import ru.volgadev.article_data.model.Article

private const val ARTICLE_DATABASE_NAME = "article-database"

@Dao
@WorkerThread
interface ArticleDao {
    @Query("SELECT * FROM article")
    fun getAll(): List<Article>

    @Query("SELECT * FROM article WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Article>

    @Insert
    fun insertAll(vararg users: Article)

    @Delete
    fun delete(user: Article)
}

@Database(entities = [Article::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): ArticleDao
}

fun getDatabase(context: Context) {
    val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java, ARTICLE_DATABASE_NAME
    ).build()
}