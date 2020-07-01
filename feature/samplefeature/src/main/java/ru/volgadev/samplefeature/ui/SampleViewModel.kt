package ru.volgadev.samplefeature.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.volgadev.sampledata.model.Article
import ru.volgadev.sampledata.repository.SampleRepository

class SampleViewModel(private val sampleRepository: SampleRepository) : ViewModel() {

    val articles: LiveData<ArrayList<Article>> = sampleRepository.articles().asLiveData()
}