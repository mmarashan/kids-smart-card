package ru.volgadev.article_galery.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.main_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.article_galery.R
import ru.volgadev.common.log.Logger


class ArticleGalleryFragment : Fragment(R.layout.main_fragment) {

    private val logger = Logger.get("ArticleGalleryFragment")

    companion object {
        fun newInstance() = ArticleGalleryFragment()
    }

    private val viewModel: ArticleGaleryViewModel by viewModel()

    interface OnItemClickListener {
        fun onClick(itemId: Long, clickedView: View)
    }

    @Volatile
    private var onItemClickListener: OnItemClickListener? = null

    @AnyThread
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("On fragment created")

        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)

        val viewAdapter = ArticleCardAdapter().apply {
            setOnItemClickListener(object : ArticleCardAdapter.OnItemClickListener {
                override fun onClick(itemId: Long, clickedView: View) {
                    onItemClickListener?.onClick(itemId, clickedView)
                }
            })
        }

        contentRecyclerView.run {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            layoutManager = staggeredGridLayoutManager
            adapter = viewAdapter
        }

        viewModel.articles.observe(viewLifecycleOwner, Observer { articles ->
            logger.debug("Set new ${articles.size} articles")
            viewAdapter.setDataset(articles)
        })
    }

}