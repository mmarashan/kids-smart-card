package ru.volgadev.article_data.storage

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.*
import ru.volgadev.article_data.model.Article
import java.util.stream.Collectors

@Dao
@WorkerThread
interface ArticleDao {
    @Query("SELECT * FROM article")
    fun getAll(): List<Article>

    @Query("SELECT * FROM article WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Article>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: Article)

    @Delete
    fun delete(user: Article)

    @Query("SELECT EXISTS(SELECT * FROM article WHERE id = :id)")
    fun isRowIsExist(id : Int) : Boolean
}

@Database(entities = [Article::class], version = 1)
@TypeConverters(ListStringConverter::class)
abstract class ArticleDatabase : RoomDatabase() {

    abstract fun userDao(): ArticleDao

    companion object {
        @Volatile
        private var INSTANCE: ArticleDatabase? = null

        private const val ARTICLE_DATABASE_NAME = "article-database.db"

        fun getInstance(context: Context): ArticleDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ArticleDatabase::class.java, ARTICLE_DATABASE_NAME
            ).build()
    }
}

private class ListStringConverter {

    private val DELIMITER = ","

    @TypeConverter
    fun fromHobbies(hobbies: List<String>): String {
        return hobbies.stream().collect(Collectors.joining(DELIMITER))
    }

    @TypeConverter
    fun toHobbies(data: String): List<String> {
        return data.split(DELIMITER)
    }
}