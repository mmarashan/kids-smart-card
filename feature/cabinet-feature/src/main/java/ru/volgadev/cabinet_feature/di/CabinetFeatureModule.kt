package ru.volgadev.cabinet_feature.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.volgadev.cabinet_feature.presentation.CabinetViewModel

val cabinetFeatureModule = module {
    viewModel { CabinetViewModel(get()) }
}