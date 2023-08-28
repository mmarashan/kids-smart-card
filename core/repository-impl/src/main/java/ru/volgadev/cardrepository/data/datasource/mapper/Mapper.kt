package ru.volgadev.cardrepository.data.datasource.mapper

import ru.volgadev.cardrepository.data.datasource.dto.CardsResponseDto
import ru.volgadev.cardrepository.data.datasource.dto.CategoriesResponseDto
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.cardrepository.domain.model.CardCategory

internal object Mapper {

    fun map(dto: CategoriesResponseDto): List<CardCategory> = dto.categories.map {
        CardCategory(
            id = it.id,
            name = it.name,
            description = it.description.orEmpty(),
            iconUrl = it.iconUrl,
            fileUrl = it.fileUrl,
            marketItemId = it.marketItemId,
            isPaid = false
        )
    }

    fun map(dto: CardsResponseDto): List<Card> = dto.cards.map {
        Card(
            id = it.id,
            tags = it.tags.orEmpty(),
            author = it.author.orEmpty(),
            title = it.title.orEmpty(),
            categoryId = it.categoryId,
            iconUrl = it.iconUrl,
            onClickSounds = it.onClickSounds.orEmpty(),
            openPhrase = it.openPhrase
        )
    }
}