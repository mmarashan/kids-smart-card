package ru.volgadev.article_repository.data.database

import androidx.room.*
import ru.volgadev.article_repository.domain.database.ArticleCategoriesDao
import ru.volgadev.article_repository.domain.database.ArticleCategoriesDatabase
import ru.volgadev.article_repository.domain.model.ArticleCategory

interface ArticleCategoriesDatabaseInterface: ArticleCategoriesDatabase {
    override fun dao(): ArticleCategoriesDaoImpl
}


@Dao
interface ArticleCategoriesDaoImpl : ArticleCategoriesDao {
    @Query("SELECT * FROM articlecategory")
    override fun getAll(): List<ArticleCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insertAll(vararg users: ArticleCategory)

    @Query("UPDATE articlecategory SET isPaid = :isPaid WHERE id == :id")
    override fun updateIsPaid(id: String, isPaid: Boolean)

    @Delete
    override fun delete(user: ArticleCategory)
}