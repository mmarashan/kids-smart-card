package ru.volgadev.sampledata.repository

import kotlinx.coroutines.flow.Flow
import ru.volgadev.sampledata.model.Article

interface SampleRepository {
    fun articles(): Flow<ArrayList<Article>>
}