package ru.volgadev.article_repository.data.datasource.mapper

import ru.volgadev.article_repository.data.datasource.dto.CategoriesResponseDto
import ru.volgadev.article_repository.domain.model.ArticleCategory

object Mapper {

    fun map(dto: CategoriesResponseDto): List<ArticleCategory> = dto.categories.map {
        ArticleCategory(
            id = it.id,
            name = it.name,
            description = it.description.orEmpty(),
            iconUrl = it.iconUrl,
            fileUrl = it.fileUrl,
            marketItemId = it.marketItemId
        )
    }
}