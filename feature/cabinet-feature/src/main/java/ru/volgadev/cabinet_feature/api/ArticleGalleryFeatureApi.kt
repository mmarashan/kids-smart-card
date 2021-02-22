package ru.volgadev.cabinet_feature.api

import androidx.fragment.app.Fragment
import ru.sberdevices.module_injector.BaseAPI
import ru.volgadev.cabinet_feature.presentation.CabinetFragment

interface CabinetFeatureApi : BaseAPI {
    fun getFragment(): Fragment
}

internal class CabinetFeatureApiImpl : CabinetFeatureApi {
    override fun getFragment(): Fragment {
        return CabinetFragment()
    }
}

