package ru.volgadev.cardrepository.data.datasource

import androidx.annotation.WorkerThread
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.cardrepository.domain.model.CardCategory

@WorkerThread
internal interface CardRemoteDataSource {
    suspend fun getCategories(): List<CardCategory>
    suspend fun getCards(category: CardCategory): List<Card>
}