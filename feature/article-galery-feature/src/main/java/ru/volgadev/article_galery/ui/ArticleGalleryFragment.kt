package ru.volgadev.article_galery.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.main_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.model.ArticleType
import ru.volgadev.article_galery.R
import ru.volgadev.common.BackgroundMediaPlayer
import ru.volgadev.common.log.Logger
import ru.volgadev.common.scaleToFitAnimatedAndBack

class ArticleGalleryFragment : Fragment(R.layout.main_fragment) {

    private val logger = Logger.get("ArticleGalleryFragment")

    companion object {
        fun newInstance() = ArticleGalleryFragment()
    }

    private val viewModel: ArticleGalleryViewModel by viewModel()

    private val musicMediaPlayer by lazy { BackgroundMediaPlayer() }
    private val cardsMediaPlayer by lazy { BackgroundMediaPlayer() }

    interface OnItemClickListener {
        fun onClick(article: Article, clickedView: View)
    }

    @Volatile
    private var onItemClickListener: OnItemClickListener? = null

    @AnyThread
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("On fragment created; savedInstanceState=$savedInstanceState")

        val viewAdapter = ArticleCardAdapter().apply {
            setOnItemClickListener(object : ArticleCardAdapter.OnItemClickListener {
                override fun onClick(itemId: Long, clickedView: View) {
                    val clickedArticle =
                        viewModel.currentArticles.value?.first { article -> article.id == itemId }
                    clickedArticle?.let { article ->
                        logger.debug("On click article ${article.id}")
                        viewModel.onClickArticle(article)
                        val startElevation = clickedView.elevation
                        clickedView.elevation = startElevation + 1
                        if (article.type == ArticleType.NO_PAGES) {
                            clickedView.scaleToFitAnimatedAndBack(1000L, 1000L, 1000L) {
                                clickedView.elevation = startElevation
                            }
                        }
                        onItemClickListener?.onClick(article, clickedView)
                    }
                }
            })
        }

        viewModel.audioToPlay.observe(viewLifecycleOwner, Observer { track ->
            val audioPath = track.filePath ?: track.url
            logger.debug("Play $audioPath")
            cardsMediaPlayer.setOnCompletionListener {
                view.postDelayed({
                    musicMediaPlayer.setVolume(1f, 1f)
                }, 700L)
            }
            musicMediaPlayer.setVolume(0.4f, 0.4f)
            cardsMediaPlayer.playAudio(
                view.context,
                Uri.parse(audioPath)
            )
        })

        contentRecyclerView.run {
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = viewAdapter
        }

        viewModel.currentArticles.observe(viewLifecycleOwner, Observer { articles ->
            logger.debug("Set new ${articles.size} articles")
            viewAdapter.setData(articles)
        })

        val categoryTagsAdapter = TagsAdapter(R.layout.category_tag).apply {
            setOnItemClickListener(object : TagsAdapter.OnItemClickListener {
                override fun onClick(item: String, clickedView: CardView) {
                    logger.debug("on click $item")
                    viewModel.onClickCategory(item)
                    onChose(item)
                }
            })
        }

        viewModel.currentCategory.observe(viewLifecycleOwner, Observer { category ->
            logger.debug("Set category $category")
            categoryTagsAdapter.onChose(category)
        })

        categoryRecyclerView.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = categoryTagsAdapter
            val dividerDrawable =
                ContextCompat.getDrawable(context, R.drawable.empty_divider_4)!!
            val dividerDecorator =
                DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL).apply {
                    setDrawable(dividerDrawable)
                }
            addItemDecoration(dividerDecorator)
        }

        viewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            categoryTagsAdapter.setData(categories)
        })

        viewModel.tracks.observe(viewLifecycleOwner, Observer { tracks ->
            logger.debug("On new ${tracks.size} tracks")
            val downloadedTracks = tracks.filter { track -> track.filePath != null }
            val trackUrl = if (downloadedTracks.isNotEmpty()) {
                logger.debug("Play downloaded")
                downloadedTracks.random().filePath!!
            } else {
                logger.debug("Play from streaming")
                if (tracks.isNotEmpty()) {
                    tracks.random().url
                } else {
                    null
                }
            }
            trackUrl?.let { url ->
                try {
                    musicMediaPlayer.playAudio(context!!, Uri.parse(url))
                } catch (e: Exception) {
                    logger.error("Exception when playing: ${e.message}")
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        logger.debug("onResume()")
        if (musicMediaPlayer.isPaused()) {
            logger.debug("Start paused playing")
            musicMediaPlayer.start()
        }
    }

    override fun onPause() {
        logger.debug("onPause()")
        musicMediaPlayer.pause()
        super.onPause()
    }
}