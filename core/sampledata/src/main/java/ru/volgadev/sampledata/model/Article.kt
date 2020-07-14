package ru.volgadev.sampledata.model

data class Article(
    val id: Long,
    val tags: List<String>,
    val title: String,
    val text: String,
    val iconUrl: String,
    val averageTimeReadingMin: Int,
    val hardLevel: Int
)