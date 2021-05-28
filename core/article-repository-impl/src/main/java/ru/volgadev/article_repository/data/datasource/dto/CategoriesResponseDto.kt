package ru.volgadev.article_repository.data.datasource.dto

import com.google.gson.annotations.SerializedName

internal data class CategoriesResponseDto(
    @SerializedName("categories")
    val categories: List<ArticleCategoryDto>
)