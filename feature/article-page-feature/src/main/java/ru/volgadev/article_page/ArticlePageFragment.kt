package ru.volgadev.article_page

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.common.log.Logger


class ArticlePageFragment : Fragment(R.layout.layout_article_page) {

    private val logger = Logger.get("ArticlePageFragment")

    companion object {
        fun newInstance() = ArticlePageFragment()
    }

    private val viewModel: ArticlePageViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("On fragment created")

        viewModel.article.observe(viewLifecycleOwner, Observer { article ->
            logger.debug("Set new ${article} article")
        })
    }

}