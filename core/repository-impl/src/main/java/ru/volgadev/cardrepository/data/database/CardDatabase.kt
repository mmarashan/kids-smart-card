package ru.volgadev.cardrepository.data.database

import kotlinx.coroutines.flow.Flow
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.cardrepository.domain.model.CardCategory

internal interface CardDatabase {
    fun dao(): CardDao
}

internal interface CardDao {

    fun cards(): List<Card>

    fun getCardsByCategory(categoryId: String): List<Card>

    fun insertAllArticles(vararg cards: Card)

    fun delete(card: Card)

    fun isCardsExists(id: Int): Boolean

    fun categories(): Flow<List<CardCategory>>

    fun insertAllCategories(vararg users: CardCategory)

    fun updateCategoryIsPaid(id: String, isPaid: Boolean)

    fun delete(user: CardCategory)
}