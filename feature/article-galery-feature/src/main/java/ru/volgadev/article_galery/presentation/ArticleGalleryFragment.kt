package ru.volgadev.article_galery.presentation

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.article_galery.databinding.GalleryFragmentLayoutBinding
import ru.volgadev.article_galery.presentation.adapter.ArticleCardAdapter
import ru.volgadev.article_galery.presentation.adapter.TagsAdapter
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.common.animateScaledVibration
import ru.volgadev.common.log.Logger
import ru.volgadev.common.scaleToFitAnimatedAndBack
import ru.volgadev.common.setVisibleWithTransition
import ru.volgadev.common.view.scrollToItemToCenter

class ArticleGalleryFragment : Fragment() {

    private val logger = Logger.get("ArticleGalleryFragment")

    private val viewModel: ArticleGalleryViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = GalleryFragmentLayoutBinding.inflate(inflater, container, false)

        val articlesAdapter = ArticleCardAdapter()

        val isPortraitOrientation =
            requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val spanCount = if (isPortraitOrientation) 2 else 3

        binding.contentRecyclerView.run {
            layoutManager = StaggeredGridLayoutManager(spanCount, LinearLayoutManager.VERTICAL)
            adapter = articlesAdapter
            itemAnimator = SlideInUpAnimator().apply {
                addDuration = CARD_ADD_ANIMATION_DURATION_MS
                removeDuration = CARD_ADD_ANIMATION_DURATION_MS
                changeDuration = CARD_ADD_ANIMATION_DURATION_MS
            }
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == SCROLL_STATE_DRAGGING) binding.showMusicControlPanelForTime()
                }
            })
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

        val categoryTagsAdapter = TagsAdapter().apply {
            setOnItemClickListener(object : TagsAdapter.OnItemClickListener {
                override fun onClick(item: String, clickedView: CardView, position: Int) {
                    binding.categoryRecyclerView.scrollToItemToCenter(position)
                    lifecycleScope.launchWhenCreated {
                        val category = viewModel.availableCategories.firstOrNull()
                            ?.firstOrNull { it.name == item }
                        category?.let { viewModel.onClickCategory(it) }
                    }
                }
            })
        }

        lifecycleScope.launchWhenResumed {
            viewModel.currentCategory.collect { categoryTagsAdapter.onChoose(it.name) }
        }

        binding.categoryRecyclerView.run {
            setHasFixedSize(true)
            adapter = categoryTagsAdapter
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

        val musicToggleButton = binding.musicControlsLayout.musicToggleButton
        musicToggleButton.setOnCheckedChangeListener { _, isChecked ->
            logger.debug("on click backgroundMusicToggleButton")
            viewModel.onToggleMusicPlayer(isChecked)
            val buttonAmplitudeSign = if (isChecked) 1 else -1
            musicToggleButton.animateScaledVibration(
                scaleAmplitude = buttonAmplitudeSign * MUSIC_BUTTON_SCALE_AMPLITUDE,
                durationMs = MUSIC_BUTTON_SCALE_DURATION_MS
            )
        }
        viewModel.onToggleMusicPlayer(musicToggleButton.isChecked)

        binding.musicControlsLayout.prevTrackButton.setOnClickListener { viewModel.onClickPreviousTrack() }
        binding.musicControlsLayout.nextTrackButton.setOnClickListener { viewModel.onClickNextTrack() }
        binding.showMusicControlPanelForTime()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        logger.debug("onResume()")
        viewModel.onToggleMusicPlayer(true)
    }

    override fun onPause() {
        logger.debug("onPause()")
        viewModel.onToggleMusicPlayer(false)
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


    private fun GalleryFragmentLayoutBinding.showMusicControlPanelForTime(){
        musicControlsLayout.root.visibility = View.VISIBLE
        musicControlsLayout.root.postDelayed({
            musicControlsLayout.root.visibility = View.GONE
        }, 2000L)
    }

    private companion object {
        const val MUSIC_BUTTON_SCALE_AMPLITUDE = 0.15f
        const val MUSIC_BUTTON_SCALE_DURATION_MS = 600L
        const val CARD_ADD_ANIMATION_DURATION_MS = 250L
    }
}