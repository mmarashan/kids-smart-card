package ru.volgadev.cardgallery.presentation

import android.content.res.Configuration
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.cardgallery.databinding.GalleryFragmentLayoutBinding
import ru.volgadev.cardgallery.presentation.adapter.ArticleCardAdapter
import ru.volgadev.cardgallery.presentation.adapter.TagsAdapter
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.common.ext.animateScaledVibration
import ru.volgadev.common.ext.scaleToFitAnimatedAndBack
import ru.volgadev.common.ext.setVisibleWithTransition
import ru.volgadev.common.log.Logger
import ru.volgadev.common.view.scrollToItemToCenter
import ru.volgadev.speaking_character.api.SpeakingCharacterApi
import ru.volgadev.speaking_character.set.gingerCat
import ru.volgadev.speaking_character.set.mole

class CardGalleryFragment : Fragment() {

    private val logger = Logger.get("ArticleGalleryFragment")

    private val viewModel: CardGalleryViewModel by viewModel()

    private var musicPanelHideDelayedJob: Job? = null
    private lateinit var musicToggleButton: ToggleButton

    private val characterManager by lazy { SpeakingCharacterApi.get(requireContext()) }
    private val characters = setOf(gingerCat, mole)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = GalleryFragmentLayoutBinding.inflate(inflater, container, false)
        musicToggleButton = binding.musicControlsLayout.musicToggleButton

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
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == SCROLL_STATE_DRAGGING) binding.showMusicControlPanelForTime()
                }
            })
        }

        var canClick = true

        articlesAdapter.setOnItemClickListener(object : ArticleCardAdapter.OnItemClickListener {

            override fun onClick(item: Card, clickedView: View, position: Int) {
                if (!canClick) return
                logger.debug("On click article ${item.id}")
                viewModel.onClickArticle(item)

                canClick = false
                highlightView(view = clickedView, onEnd = { canClick = true })
                showSpeakingCharacter()
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

        musicToggleButton.setOnCheckedChangeListener { _, isChecked ->
            logger.debug("on click backgroundMusicToggleButton")
            viewModel.onToggleMusicPlayer(isChecked)
            val buttonAmplitudeSign = if (isChecked) 1 else -1
            musicToggleButton.animateScaledVibration(
                scaleAmplitude = buttonAmplitudeSign * MUSIC_BUTTON_SCALE_AMPLITUDE,
                durationMs = MUSIC_BUTTON_SCALE_DURATION_MS
            )
        }

        binding.musicControlsLayout.prevTrackButton.setOnClickListener { viewModel.onClickPreviousTrack() }
        binding.musicControlsLayout.nextTrackButton.setOnClickListener { viewModel.onClickNextTrack() }
        binding.showMusicControlPanelForTime()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        logger.debug("onResume()")
        viewModel.onToggleMusicPlayer(musicToggleButton.isChecked)
    }

    override fun onPause() {
        logger.debug("onPause()")
        viewModel.onToggleMusicPlayer(false)
        musicPanelHideDelayedJob?.cancel()
        musicPanelHideDelayedJob = null
        super.onPause()
    }

    private fun highlightView(view: View, onEnd: () -> Unit) {
        val startElevation = view.elevation
        view.elevation = startElevation + 1
        view.scaleToFitAnimatedAndBack(
            SCALE_ANUMATION_DURATION_MS / 3,
            SCALE_ANUMATION_DURATION_MS / 3,
            SCALE_ANUMATION_DURATION_MS / 3,
            0.85f
        ) {
            view.elevation = startElevation
            onEnd.invoke()
        }
    }


    private fun GalleryFragmentLayoutBinding.showMusicControlPanelForTime() {

        musicControlsLayout.root.setVisibleWithTransition(
            View.VISIBLE,
            Slide(Gravity.BOTTOM),
            MUSIC_PANEL_TRANSITION_DURATION_MS,
            root
        )

        musicPanelHideDelayedJob?.cancel()
        musicPanelHideDelayedJob = lifecycleScope.launchWhenResumed {
            delay(MUSIC_PANEL_VISIBILITY_DURATION_MS)
            if (isActive) musicControlsLayout.root.setVisibleWithTransition(
                View.GONE,
                Slide(Gravity.BOTTOM),
                MUSIC_PANEL_TRANSITION_DURATION_MS,
                root
            )
        }
    }

    private fun showSpeakingCharacter() = characterManager.show(
        activity = requireActivity(),
        character = characters.random(),
        showTimeMs = CHARACTER_SHOW_TIME_MS
    )

    private companion object {
        const val MUSIC_BUTTON_SCALE_AMPLITUDE = 0.15f
        const val MUSIC_BUTTON_SCALE_DURATION_MS = 600L
        const val CARD_ADD_ANIMATION_DURATION_MS = 250L

        const val MUSIC_PANEL_TRANSITION_DURATION_MS = 600L
        const val MUSIC_PANEL_VISIBILITY_DURATION_MS = 2000L
        const val CHARACTER_SHOW_TIME_MS = 3000L
        const val SCALE_ANUMATION_DURATION_MS = 3000L
    }
}