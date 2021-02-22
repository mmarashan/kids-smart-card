package ru.volgadev.article_data.domain

import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

internal interface ArticleDatabase {
    fun dao(): ArticleDao
}

internal interface ArticleCategoriesDatabase {
    fun dao(): ArticleChannelsDao
}

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

@Dao
@WorkerThread
interface ArticleChannelsDao {
    @Query("SELECT * FROM articlecategory")
    fun getAll(): List<ArticleCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: ArticleCategory)

    @Query("UPDATE articlecategory SET isPaid = :isPaid WHERE id == :id")
    fun updateIsPaid(id: String, isPaid: Boolean)

    @Delete
    fun delete(user: ArticleCategory)
}