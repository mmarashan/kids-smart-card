package ru.volgadev.article_repository.domain.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Article(
    @PrimaryKey
    val id: Long,
    val tags: List<String> = listOf(),
    val author: String,
    val title: String,
    val categoryId: String,
    val iconUrl: String? = null,
    val onClickSounds: List<String> = listOf(),
    val openPhrase: String?
)

@Entity
data class ArticleCategory(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String? = null,
    val fileUrl: String,
    val marketItemId: String? = null,
    var isPaid: Boolean = true
) {
    @Ignore
    val isFree = marketItemId == null
}