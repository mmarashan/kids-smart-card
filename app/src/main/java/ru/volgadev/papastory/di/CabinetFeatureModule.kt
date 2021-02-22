package ru.volgadev.papastory.di

import dagger.Module
import dagger.Provides
import ru.volgadev.article_data.api.ArticleRepositoryApi
import ru.volgadev.cabinet_feature.api.CabinetFeatureComponentHolder
import ru.volgadev.cabinet_feature.api.CabinetFeatureDependencies

@Module(
    includes = [ArticleRepositoryModule::class]
)
interface CabinetFeatureModule {

    companion object {
        @Provides
        fun providesCabinetFeatureDependencies(
            articleRepositoryApi: ArticleRepositoryApi
        ): CabinetFeatureDependencies =
            CabinetFeatureDependencies(
                articleRepository = articleRepositoryApi.getArticleRepository()
            )

        @Provides
        fun providesCabinetFeatureComponentHolder(dependencies: CabinetFeatureDependencies): CabinetFeatureComponentHolder =
            CabinetFeatureComponentHolder().apply {
                init(dependencies)
            }

        @Provides
        fun providesCabinetFeatureApi(holder: CabinetFeatureComponentHolder) = holder.get()
    }
}
