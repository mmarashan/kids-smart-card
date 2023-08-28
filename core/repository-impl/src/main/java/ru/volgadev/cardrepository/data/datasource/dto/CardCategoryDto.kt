package ru.volgadev.cardrepository.data.datasource.dto

import com.google.gson.annotations.SerializedName

internal data class CardCategoryDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("iconUrl")
    val iconUrl: String? = null,
    @SerializedName("fileUrl")
    val fileUrl: String,
    @SerializedName("marketItemId")
    val marketItemId: String? = null
)