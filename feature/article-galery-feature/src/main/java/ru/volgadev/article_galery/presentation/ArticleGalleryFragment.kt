package ru.volgadev.article_galery.presentation

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.article_galery.R
import ru.volgadev.article_galery.presentation.adapter.ArticleCardAdapter
import ru.volgadev.article_galery.presentation.adapter.TagsAdapter
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.common.BackgroundMediaPlayer
import ru.volgadev.common.animateScaledVibration
import ru.volgadev.common.log.Logger
import ru.volgadev.common.scaleToFitAnimatedAndBack
import ru.volgadev.common.view.scrollToItemToCenter

// TODO: вынести константы
class ArticleGalleryFragment : Fragment(R.layout.main_fragment) {

    private val logger = Logger.get("ArticleGalleryFragment")

    // TODO: рефакторить работу с плеерами (мб в свой интерактор их)
    private val musicMediaPlayer by lazy { BackgroundMediaPlayer() }
    private val cardsMediaPlayer by lazy { BackgroundMediaPlayer() }

    private val viewModel: ArticleGalleryViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            itemAnimator = SlideInUpAnimator().apply {
                addDuration = 248
                removeDuration = 200
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
            viewModel.currentCategory.collect { categoryTagsAdapter.onChose(it.name) }
        }

        categoryRecyclerView.run {
            setHasFixedSize(true)
            adapter = categoryTagsAdapter
            val dividerDrawable = ContextCompat.getDrawable(context, R.drawable.empty_divider_4)!!
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
                    tracks.randomOrNull()?.url
                }
                trackUrl?.let { url ->
                    try {
                        musicMediaPlayer.playAudio(requireContext(), Uri.parse(url))
                        musicToggleButton.isChecked = true
                        musicToggleButton.isVisible = true
                    } catch (e: Exception) {
                        logger.error("Exception when playing: ${e.message}")
                    }
                }
            }
        }

        musicToggleButton.isVisible = false
        musicToggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
            logger.debug("on click backgroundMusicToggleButton")
            if (isChecked) {
                musicMediaPlayer.start()
                musicToggleButton.animateScaledVibration(
                    scaleAmplitude = MUSIC_BUTTON_SCALE_AMPLITUDE,
                    durationMs = MUSIC_BUTTON_SCALE_DURATION_MS
                )
            } else {
                musicToggleButton.animateScaledVibration(
                    scaleAmplitude = -MUSIC_BUTTON_SCALE_AMPLITUDE,
                    durationMs = MUSIC_BUTTON_SCALE_DURATION_MS
                )
                musicMediaPlayer.pause()
            }
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

    private companion object {
        const val MUSIC_BUTTON_SCALE_AMPLITUDE = 0.2f
        const val MUSIC_BUTTON_SCALE_DURATION_MS = 800L
    }
}