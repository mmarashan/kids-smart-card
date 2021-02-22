package ru.volgadev.article_data.storage

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.*
import ru.volgadev.article_data.domain.Article
import ru.volgadev.article_data.domain.ArticleDatabase

@Dao
@WorkerThread
internal interface ArticleDao {
    @Query("SELECT * FROM article")
    fun getAll(): List<Article>

    @Query("SELECT * FROM article WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Article>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: Article)

    @Delete
    fun delete(user: Article)

    @Query("SELECT EXISTS(SELECT * FROM article WHERE id = :id)")
    fun isRowIsExist(id: Int): Boolean
}

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