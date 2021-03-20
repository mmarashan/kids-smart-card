package ru.volgadev.article_data.domain

import androidx.room.*

@Entity
data class Article(
    @PrimaryKey
    val id: Long,
    val tags: List<String> = listOf(),
    val author: String,
    val title: String,
    val categoryId: String,
    val type: ArticleType,
    val pagesFile: String? = null,
    val iconUrl: String? = null,
    val onClickSounds: List<String> = listOf(),
    val averageTimeReadingMin: Int? = 0,
    val timestamp: Long,
    val openPhrase: String?
)

enum class ArticleType {
    NO_PAGES
}

// TODO: come back when restart using articles with page
// @Entity(indices = [Index(value = ["article_id"])])
data class ArticlePage(
    @ColumnInfo(name = "article_id")
    val articleId: Long,
    val pageNum: Int,
    val type: PageType,
    val title: String? = null,
    val text: String? = null,
    val imageUrl: String? = null,
    val soundUrl: String? = null,
    val allowBackgroundSound: Boolean = true
)

enum class PageType {
    TITLE_WITH_IMAGE, TEXT_WITH_IMAGE, ONLY_TEXT
}

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