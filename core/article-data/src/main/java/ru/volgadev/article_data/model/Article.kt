package ru.volgadev.article_data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Article(
    @PrimaryKey
    val id: Long,
    val tags: List<String> = listOf(),
    val title: String,
    val text: String,
    val iconUrl: String? = null,
    val averageTimeReadingMin: Int? = 0,
    val hardLevel: Int? = 0,
    val timestamp: Long
)