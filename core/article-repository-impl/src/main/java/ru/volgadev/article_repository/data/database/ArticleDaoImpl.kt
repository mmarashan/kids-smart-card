package ru.volgadev.article_repository.data.database

import androidx.room.*
import ru.volgadev.article_repository.domain.database.ArticleDao
import ru.volgadev.article_repository.domain.database.ArticleDatabase
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory

internal interface ArticleDatabaseInterface: ArticleDatabase {
    override fun dao(): ArticleDaoImpl
}

@Dao
internal interface ArticleDaoImpl : ArticleDao {
    @Query("SELECT * FROM article")
    override fun getAllArticles(): List<Article>

    @Query("SELECT * FROM article WHERE id IN (:userIds)")
    override fun loadAllByIds(userIds: IntArray): List<Article>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insertAllArticles(vararg users: Article)

    @Delete
    override fun delete(user: Article)

    @Query("SELECT EXISTS(SELECT * FROM article WHERE id = :id)")
    override fun isArticleExists(id: Int): Boolean

    @Query("SELECT * FROM articlecategory")
    override fun getAllCategories(): List<ArticleCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insertAllCategories(vararg users: ArticleCategory)

    @Query("UPDATE articlecategory SET isPaid = :isPaid WHERE id == :id")
    override fun updateCategoryIsPaid(id: String, isPaid: Boolean)

    @Delete
    override fun delete(user: ArticleCategory)
}
