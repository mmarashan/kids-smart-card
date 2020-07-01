package ru.volgadev.appsample.data.repository

import android.content.Context
import ru.volgadev.samplefeature.data.repository.SampleRepository
import ru.volgadev.samplefeature.data.repository.SampleRepositoryImpl

object Repositories {

    fun getSampleRepository(context: Context): SampleRepository {
        return SampleRepositoryImpl(context)
    }

}