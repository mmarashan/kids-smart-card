package ru.volgadev.article_repository.data.datasource.mapper

import ru.volgadev.article_repository.data.datasource.dto.ArticlesResponseDto
import ru.volgadev.article_repository.data.datasource.dto.CategoriesResponseDto
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory

internal object Mapper {

    fun map(dto: CategoriesResponseDto): List<ArticleCategory> = dto.categories.map {
        ArticleCategory(
            id = it.id,
            name = it.name,
            description = it.description.orEmpty(),
            iconUrl = it.iconUrl,
            fileUrl = it.fileUrl,
            marketItemId = it.marketItemId,
            isPaid = false
        )
    }

    fun map(dto: ArticlesResponseDto): List<Article> = dto.articles.map {
        Article(
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