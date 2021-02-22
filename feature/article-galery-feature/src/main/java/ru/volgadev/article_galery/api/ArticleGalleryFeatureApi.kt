package ru.volgadev.article_galery.api

import android.content.Context
import androidx.fragment.app.Fragment
import ru.sberdevices.module_injector.BaseAPI
import ru.volgadev.article_galery.presentation.ArticleGalleryFragment

interface ArticleGalleryFeatureApi: BaseAPI {
    fun getFragment(): Fragment
}

internal class ArticleGalleryFeatureApiImpl: ArticleGalleryFeatureApi {
    override fun getFragment(): Fragment {
        return ArticleGalleryFragment()
    }
}

