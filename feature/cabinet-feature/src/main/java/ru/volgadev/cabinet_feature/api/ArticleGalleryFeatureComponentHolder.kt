package ru.volgadev.cabinet_feature.api

import ru.sberdevices.module_injector.BaseDependencies
import ru.sberdevices.module_injector.ComponentHolder
import ru.volgadev.article_data.domain.ArticleRepository
import ru.volgadev.cabinet_feature.presentation.CabinetViewModelFactory

class CabinetFeatureDependencies(
    val articleRepository: ArticleRepository
) : BaseDependencies

class CabinetFeatureComponentHolder :
    ComponentHolder<CabinetFeatureApi, CabinetFeatureDependencies> {

    private var articleRepository: ArticleRepository? = null
    private var cabinetFeatureApi: CabinetFeatureApiImpl? = null

    override fun init(dependencies: CabinetFeatureDependencies) {
        articleRepository = dependencies.articleRepository
    }

    @Synchronized
    override fun get(): CabinetFeatureApi {
        val articleRepository = articleRepository
        checkNotNull(articleRepository) { "articleRepository was not initialized!" }
        if (cabinetFeatureApi == null) {
            CabinetViewModelFactory.inject(articleRepository)
            cabinetFeatureApi = CabinetFeatureApiImpl()
        }
        return cabinetFeatureApi!!
    }

    override fun clear() {
        articleRepository = null
        cabinetFeatureApi = null
        CabinetViewModelFactory.clear()
    }
}