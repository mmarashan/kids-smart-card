package ru.volgadev.cardrepository.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.cardrepository.domain.model.CardCategory

internal interface CardDatabaseInterface : CardDatabase {
    override fun dao(): CardDaoImpl
}

@Dao
internal interface CardDaoImpl : CardDao {
    @Query("SELECT * FROM card")
    override fun cards(): List<Card>

    @Query("SELECT * FROM card where categoryId = :categoryId")
    override fun getCardsByCategory(categoryId: String): List<Card>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insertAllArticles(vararg cards: Card)

    @Delete
    override fun delete(card: Card)

    @Query("SELECT EXISTS(SELECT * FROM card WHERE id = :id)")
    override fun isCardsExists(id: Int): Boolean

    @Query("SELECT * FROM cardcategory")
    override fun categories(): Flow<List<CardCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insertAllCategories(vararg items: CardCategory)

    @Query("UPDATE cardcategory SET isPaid = :isPaid WHERE id == :id")
    override fun updateCategoryIsPaid(id: String, isPaid: Boolean)

    @Delete
    override fun delete(user: CardCategory)
}
