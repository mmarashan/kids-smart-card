package ru.volgadev.article_data.api

import android.content.Context
import ru.sberdevices.module_injector.BaseDependencies
import ru.sberdevices.module_injector.ComponentHolder
import ru.volgadev.article_data.data.ArticleBackendApiImpl
import ru.volgadev.article_data.storage.ArticleCategoriesDatabaseImpl
import ru.volgadev.article_data.data.ArticleDatabaseImpl
import ru.volgadev.pay_lib.PaymentManager

class ArticleRepositoryDependencies(val context: Context, val paymentManager: PaymentManager) : BaseDependencies

class ArticleRepositoryComponentHolder : ComponentHolder<ArticleRepositoryApi, ArticleRepositoryDependencies> {

    private var articleRepositoryApi: ArticleRepositoryApi? = null
    private var dependencies: ArticleRepositoryDependencies? = null

    override fun init(dependencies: ArticleRepositoryDependencies) {
        this.dependencies = dependencies
    }

    override fun get(): ArticleRepositoryApi {
        val dependencies = dependencies
        checkNotNull(dependencies)
        var articleRepositoryApiImpl = articleRepositoryApi
        if (articleRepositoryApiImpl == null) {
            articleRepositoryApiImpl = ArticleRepositoryApiImpl(
                articleBackendApi = ArticleBackendApiImpl(),
                paymentManager = dependencies.paymentManager,
                articlesDatabase = ArticleDatabaseImpl.getInstance(dependencies.context.applicationContext),
                categoriesDatabase = ArticleCategoriesDatabaseImpl.getInstance(dependencies.context.applicationContext)
            )
            this.articleRepositoryApi = articleRepositoryApiImpl
            return articleRepositoryApiImpl
        } else {
            return articleRepositoryApiImpl
        }
    }

    override fun reset() {
        articleRepositoryApi = null
        dependencies = null
    }
}