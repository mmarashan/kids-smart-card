package ru.volgadev.cardrepository.data.datasource.dto

import com.google.gson.annotations.SerializedName

internal data class CardDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("tags")
    val tags: List<String>? = listOf(),
    @SerializedName("author")
    val author: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("categoryId")
    val categoryId: String,
    @SerializedName("iconUrl")
    val iconUrl: String? = null,
    @SerializedName("onClickSounds")
    val onClickSounds: List<String>? = listOf(),
    @SerializedName("openPhrase")
    val openPhrase: String? = null
)