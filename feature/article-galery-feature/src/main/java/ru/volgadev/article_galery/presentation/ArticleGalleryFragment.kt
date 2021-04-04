package ru.volgadev.article_galery.presentation

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import ru.volgadev.article_galery.R
import ru.volgadev.article_galery.presentation.adapter.ArticleCardAdapter
import ru.volgadev.article_galery.presentation.adapter.TagsAdapter
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.common.BackgroundMediaPlayer
import ru.volgadev.common.log.Logger
import ru.volgadev.common.scaleToFitAnimatedAndBack
import ru.volgadev.common.view.scrollToItemToCenter

// TODO: вынести константы
class ArticleGalleryFragment : Fragment(R.layout.main_fragment) {

    private val logger = Logger.get("ArticleGalleryFragment")

    // TODO: рефакторить работу с плеерами (мб в свой интерактор их)
    private val musicMediaPlayer by lazy { BackgroundMediaPlayer() }
    private val cardsMediaPlayer by lazy { BackgroundMediaPlayer() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel =
            ViewModelProvider(
                this,
                ArticleGalleryViewModelFactory
            ).get(ArticleGalleryViewModel::class.java)

        val articlesAdapter = ArticleCardAdapter()

        lifecycleScope.launchWhenResumed {
            viewModel.trackToPlaying.collect { track ->
                val audioPath = track.filePath ?: track.url
                logger.debug("Play $audioPath")
                cardsMediaPlayer.setOnCompletionListener {
                    view.postDelayed({
                        musicMediaPlayer.setVolume(1f, 1f)
                    }, 700L)
                }
                musicMediaPlayer.setVolume(0.4f, 0.4f)
                cardsMediaPlayer.playAudio(view.context, Uri.parse(audioPath))
            }
        }

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

            override fun onClick(item: Article, clickedView: View, position: Int) {
                if (!canClick) return
                logger.debug("On click article ${item.id}")
                viewModel.onClickArticle(item)

                canClick = false
                highlightView(view = clickedView, onEnd = {
                    canClick = true
                })
            }
        })

        lifecycleScope.launchWhenResumed {
            viewModel.currentArticles.collect { articles ->
                articlesAdapter.setData(articles)
                canClick = true
            }
        }

        val categoryTagsAdapter = TagsAdapter(R.layout.category_tag).apply {
            setOnItemClickListener(object : TagsAdapter.OnItemClickListener {
                override fun onClick(item: String, clickedView: CardView, position: Int) {
                    categoryRecyclerView.scrollToItemToCenter(position)
                    lifecycleScope.launchWhenCreated {
                        val category = viewModel.availableCategories.firstOrNull()
                            ?.firstOrNull { it.name == item }
                        category?.let { cat ->
                            viewModel.onClickCategory(cat)
                        }
                    }
                }
            })
        }

        lifecycleScope.launchWhenResumed {
            viewModel.currentCategory.collect {
                categoryTagsAdapter.onChose(it.name)
            }
        }

        categoryRecyclerView.run {
            setHasFixedSize(true)
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

        lifecycleScope.launchWhenResumed {
            viewModel.availableCategories.collect { categories ->
                logger.debug("On load categories: ${categories.size}")
                val categoryNames = categories.map { it.name }
                categoryTagsAdapter.setData(categoryNames)
                if (categoryTagsAdapter.getChosenTag() == null) {
                    categories.firstOrNull()?.let { firstCategory ->
                        viewModel.onClickCategory(firstCategory)
                    }
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.tracks.collect { tracks ->
                // TODO: refactor work with players. remove from view!
                val downloadedTracks = tracks.filter { track -> track.filePath != null }
                val trackUrl = if (downloadedTracks.isNotEmpty()) {
                    downloadedTracks.random().filePath
                } else {
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
            }
        }

        backgroundMusicToggleButton.isVisible = false
        backgroundMusicToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) musicMediaPlayer.start() else musicMediaPlayer.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        logger.debug("onResume()")
        if (musicMediaPlayer.isPaused()) musicMediaPlayer.start()

    }

    override fun onPause() {
        logger.debug("onPause()")
        musicMediaPlayer.pause()
        super.onPause()
    }

    private fun highlightView(view: View, onEnd: () -> Unit) {
        val startElevation = view.elevation
        view.elevation = startElevation + 1
        view.scaleToFitAnimatedAndBack(
            1000L,
            1000L,
            1000L,
            0.75f
        ) {
            view.elevation = startElevation
            onEnd.invoke()
        }
    }
}