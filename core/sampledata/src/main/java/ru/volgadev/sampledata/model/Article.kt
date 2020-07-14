package ru.volgadev.sampledata.model

data class Article(
    val id: Long,
    val tags: List<String> = listOf(),
    val title: String,
    val text: String,
    val iconUrl: String? = null,
    val averageTimeReadingMin: Int? = 0,
    val hardLevel: Int? = 0
)