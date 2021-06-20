package ru.volgadev.cardrepository.domain

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.SharedFlow
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.cardrepository.domain.model.CardCategory

@WorkerThread
interface CardRepository {

    val categories: SharedFlow<List<CardCategory>>

    suspend fun getCategoryCards(category: CardCategory): List<Card>

    suspend fun requestPaymentForCategory(category: CardCategory)

    suspend fun consumePurchase(itemId: String)

    fun dispose()
}