package ru.volgadev.article_data.api

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import ru.sberdevices.module_injector.BaseAPI
import ru.volgadev.article_data.domain.ArticleBackendApi
import ru.volgadev.article_data.domain.ArticleCategoriesDatabase
import ru.volgadev.article_data.domain.ArticleDatabase
import ru.volgadev.article_data.domain.ArticleRepository
import ru.volgadev.article_data.domain.ArticleRepositoryImpl
import ru.volgadev.pay_lib.PaymentManager

interface ArticleRepositoryApi : BaseAPI {
    fun getArticleRepository(): ArticleRepository
}

internal class ArticleRepositoryApiImpl(
    private val articleBackendApi: ArticleBackendApi,
    // TODO: remove PaymentManager dependency
    private val paymentManager: PaymentManager,
    private val articlesDatabase: ArticleDatabase,
    private val categoriesDatabase: ArticleCategoriesDatabase
) : ArticleRepositoryApi {

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun getArticleRepository(): ArticleRepository =
        ArticleRepositoryImpl(articleBackendApi, paymentManager, articlesDatabase, categoriesDatabase)
}