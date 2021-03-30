package ru.volgadev.papastory.di.feature

import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.volgadev.article_data.domain.ArticleRepository
import ru.volgadev.cabinet_feature.api.CabinetFeatureComponentHolder
import ru.volgadev.cabinet_feature.api.CabinetFeatureDependencies
import ru.volgadev.papastory.di.core.ArticleRepositoryModule

@Module(
    includes = [ArticleRepositoryModule::class]
)
object CabinetFeatureModule {

    @Provides
    fun providesCabinetFeatureDependencies(
        articleRepository: ArticleRepository
    ): CabinetFeatureDependencies = CabinetFeatureDependencies(
        articleRepository = articleRepository
    )

    @Provides
    fun providesCabinetFeatureComponentHolder() = CabinetFeatureComponentHolder()
}
