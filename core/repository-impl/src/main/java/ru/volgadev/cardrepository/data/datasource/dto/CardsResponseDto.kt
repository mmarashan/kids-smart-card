package ru.volgadev.cardrepository.data.datasource.dto

import com.google.gson.annotations.SerializedName

internal data class CardsResponseDto(
    @SerializedName("articles")
    val cards: List<CardDto>
)