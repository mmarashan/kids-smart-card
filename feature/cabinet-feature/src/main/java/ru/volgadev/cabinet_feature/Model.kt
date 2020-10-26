package ru.volgadev.cabinet_feature

import ru.volgadev.article_data.model.ArticleCategory

data class MarketCategory(
    val category: ArticleCategory,
    val isFree: Boolean = true,
    var isPaid: Boolean = false
)