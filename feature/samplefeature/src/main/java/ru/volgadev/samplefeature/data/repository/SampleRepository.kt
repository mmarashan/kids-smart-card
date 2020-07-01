package ru.volgadev.samplefeature.data.repository

import kotlinx.coroutines.flow.Flow
import ru.volgadev.samplefeature.data.model.Article

// TODO: move to other module in core
interface SampleRepository {
    fun articles(): Flow<ArrayList<Article>>
}