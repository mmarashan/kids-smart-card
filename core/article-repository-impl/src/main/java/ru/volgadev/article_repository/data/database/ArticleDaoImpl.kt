package ru.volgadev.article_repository.data.database

import androidx.room.*
import ru.volgadev.article_repository.domain.database.ArticleDao
import ru.volgadev.article_repository.domain.database.ArticleDatabase
import ru.volgadev.article_repository.domain.model.Article

internal interface ArticleDatabaseInterface: ArticleDatabase {
    override fun dao(): ArticleDaoImpl
}

@Dao
internal interface ArticleDaoImpl : ArticleDao {
    @Query("SELECT * FROM article")
    override fun getAll(): List<Article>

    @Query("SELECT * FROM article WHERE id IN (:userIds)")
    override fun loadAllByIds(userIds: IntArray): List<Article>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insertAll(vararg users: Article)

    @Delete
    override fun delete(user: Article)

    @Query("SELECT EXISTS(SELECT * FROM article WHERE id = :id)")
    override fun isRowIsExist(id: Int): Boolean
}
