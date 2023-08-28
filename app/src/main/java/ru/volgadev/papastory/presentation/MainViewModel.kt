package ru.volgadev.papastory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val screenFlow = MutableSharedFlow<AppScreen>(replay = 1)

    val currentScreen: Flow<AppScreen> = screenFlow.distinctUntilChanged()

    fun onOpenScreenIntent(screen: AppScreen) = viewModelScope.launch {
        screenFlow.emit(screen)
    }
}