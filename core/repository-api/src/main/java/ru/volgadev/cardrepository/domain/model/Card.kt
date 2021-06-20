package ru.volgadev.cardrepository.domain.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Card(
    @PrimaryKey
    val id: Long,
    val tags: List<String>,
    val author: String,
    val title: String,
    val categoryId: String,
    val iconUrl: String?,
    val onClickSounds: List<String>,
    val openPhrase: String?
)

@Entity
data class CardCategory(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val fileUrl: String,
    val marketItemId: String?,
    var isPaid: Boolean
) {
    @Ignore
    val isFree = marketItemId == null
}