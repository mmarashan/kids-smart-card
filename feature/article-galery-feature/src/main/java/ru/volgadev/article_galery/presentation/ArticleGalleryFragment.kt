package ru.volgadev.article_galery.presentation

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import androidx.annotation.AnyThread
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.main_fragment.*
import ru.volgadev.article_repository.domain.Article
import ru.volgadev.article_repository.domain.ArticleType
import ru.volgadev.article_galery.R
import ru.volgadev.common.BackgroundMediaPlayer
import ru.volgadev.common.log.Logger
import ru.volgadev.common.scaleToFitAnimatedAndBack
import ru.volgadev.common.view.scrollToItemToCenter

class ArticleGalleryFragment : Fragment(R.layout.main_fragment) {

    private val logger = Logger.get("ArticleGalleryFragment")

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

        val viewModel =
            ViewModelProvider(this, ArticleGalleryViewModelFactory).get(ArticleGalleryViewModel::class.java)

        val articlesAdapter = ArticleCardAdapter(view.context)

        viewModel.audioToPlay.observe(viewLifecycleOwner, { track ->
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

        val isPortraitOrientation =
            requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val spanCount = if (isPortraitOrientation) 2 else 3

        contentRecyclerView.run {
            layoutManager = StaggeredGridLayoutManager(spanCount, LinearLayoutManager.VERTICAL)
            adapter = articlesAdapter
            overScrollMode = OVER_SCROLL_NEVER
            itemAnimator = SlideInUpAnimator().apply {
                addDuration = 248
                removeDuration = 200
                moveDuration = 200
                changeDuration = 0
            }
        }

        var canClick = true

        articlesAdapter.setOnItemClickListener(object : ArticleCardAdapter.OnItemClickListener {

            override fun onClick(itemId: Long, clickedView: View, position: Int) {
                if (!canClick) return
                val clickedArticle =
                    viewModel.currentArticles.value?.first { article -> article.id == itemId }
                clickedArticle?.let { article ->
                    logger.debug("On click article ${article.id}")
                    viewModel.onClickArticle(article)
                    val startElevation = clickedView.elevation
                    clickedView.elevation = startElevation + 1
                    if (article.type == ArticleType.NO_PAGES) {
                        canClick = false
                        clickedView.scaleToFitAnimatedAndBack(
                            1000L,
                            1000L,
                            1000L,
                            0.75f
                        ) {
                            canClick = true
                            clickedView.elevation = startElevation
                        }
                    }
                    onItemClickListener?.onClick(article, clickedView)
                }
            }
        })

        viewModel.currentArticles.observe(viewLifecycleOwner, { articles ->
            logger.debug("Set new ${articles.size} articles")
            articlesAdapter.setData(articles)
            canClick = true
        })

        val categoryTagsAdapter = TagsAdapter(view.context, R.layout.category_tag).apply {
            setOnItemClickListener(object : TagsAdapter.OnItemClickListener {
                override fun onClick(item: String, clickedView: CardView, position: Int) {
                    logger.debug("on click $item")
                    categoryRecyclerView.scrollToItemToCenter(position)
                    val category =
                        viewModel.availableCategories.value?.first { c -> c.name == item }
                    category?.let { cat ->
                        viewModel.onClickCategory(cat)
                    }
                }
            })
        }

        viewModel.currentCategory.observe(viewLifecycleOwner, { category ->
            logger.debug("Set category ${category.id}")
            categoryTagsAdapter.onChose(category.name)
        })

        categoryRecyclerView.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            overScrollMode = OVER_SCROLL_NEVER
            adapter = categoryTagsAdapter
            val dividerDrawable =
                ContextCompat.getDrawable(context, R.drawable.empty_divider_4)!!
            val dividerDecorator =
                DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL).apply {
                    setDrawable(dividerDrawable)
                }
            addItemDecoration(dividerDecorator)
            itemAnimator = null
        }

        viewModel.availableCategories.observe(viewLifecycleOwner, { categories ->
            logger.debug("On load categories: ${categories.size}")
            val categoryNames = categories.map { category -> category.name }
            categoryTagsAdapter.setData(categoryNames)
            if (categoryTagsAdapter.getChosenTag() == null) {
                categories.firstOrNull()?.let { firstCategory ->
                    viewModel.onClickCategory(firstCategory)
                }
            }
        })

        viewModel.tracks.observe(viewLifecycleOwner, { tracks ->
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
                    musicMediaPlayer.playAudio(requireContext(), Uri.parse(url))
                    backgroundMusicToggleButton.isChecked = true
                    backgroundMusicToggleButton.isVisible = true
                } catch (e: Exception) {
                    logger.error("Exception when playing: ${e.message}")
                }
            }
        })

        backgroundMusicToggleButton.isVisible = false
        backgroundMusicToggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
            logger.debug("on click backgroundMusicToggleButton")
            if (isChecked) {
                musicMediaPlayer.start()
            } else {
                musicMediaPlayer.pause()
            }
        }
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