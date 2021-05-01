package ru.volgadev.article_repository.data.datasource.dto

import com.google.gson.annotations.SerializedName

data class ArticlesResponseDto(
    @SerializedName("articles")
    val articles: List<ArticleDto>
)