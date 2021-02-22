package ru.volgadev.article_page.api

import androidx.fragment.app.Fragment
import ru.sberdevices.module_injector.BaseAPI
import ru.volgadev.article_page.presentation.ArticlePageFragment

interface ArticlePageFeatureApi : BaseAPI {
    fun getFragment(): Fragment
}

internal class ArticlePageFeatureApiImpl : ArticlePageFeatureApi {
    override fun getFragment(): ArticlePageFragment {
        return ArticlePageFragment()
    }
}

