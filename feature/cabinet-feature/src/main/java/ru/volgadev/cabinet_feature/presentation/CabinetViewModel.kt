package ru.volgadev.cabinet_feature.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.volgadev.cardrepository.domain.CardRepository
import ru.volgadev.cardrepository.domain.model.CardCategory
import ru.volgadev.common.BuildConfig
import ru.volgadev.common.log.Logger

internal class CabinetViewModel(
    private val cardRepository: CardRepository
) : ViewModel() {

    private val logger = Logger.get("CabinetViewModel")

    private val _categories = MutableLiveData<List<CardCategory>>()
    val categories: LiveData<List<CardCategory>> = _categories

    init {
        logger.debug("init")
        viewModelScope.launch {
            cardRepository.categories.collect { categories ->
                logger.debug("On update categories $categories")
                _categories.postValue(categories)
            }
        }
    }

    fun onReadyToPayment(category: CardCategory) {
        logger.debug("onReadyToPayment ${category.name}, marketItemId = ${category.marketItemId}, isPaid = ${category.isPaid}")
        category.marketItemId?.let { itemId ->
            if (!category.isPaid) {
                viewModelScope.launch { cardRepository.requestPaymentForCategory(category) }
            } else {
                viewModelScope.launch {
                    if (BuildConfig.DEBUG) {
                        logger.debug("debug consume purchase $itemId")
                        cardRepository.consumePurchase(itemId)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        logger.debug("onCleared()")
        super.onCleared()
    }
}