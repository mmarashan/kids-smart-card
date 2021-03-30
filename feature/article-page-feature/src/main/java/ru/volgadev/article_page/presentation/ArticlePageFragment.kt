package ru.volgadev.article_page.presentation

import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.layout_article_page.*
import ru.volgadev.article_page.ArticlePageViewModel
import ru.volgadev.article_page.R
import ru.volgadev.common.BackgroundMediaPlayer
import ru.volgadev.common.log.Logger
import ru.volgadev.common.runLevitateAnimation
import ru.volgadev.common.setVisibleWithTransition
import java.io.File

const val ITEM_ID_KEY = "ITEM_ID"

class ArticlePageFragment : Fragment(R.layout.layout_article_page) {

    private val logger = Logger.get("ArticlePageFragment")

    private var mediaPlayer: BackgroundMediaPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("On fragment created")

        requireArguments()
        val viewModel = ViewModelProvider(this, ArticlePageViewModelFactory).get(ArticlePageViewModel::class.java)

        val args = arguments
        if (args != null && args.containsKey(ITEM_ID_KEY)) {
            val itemId = args.getLong(ITEM_ID_KEY)
            viewModel.onChooseArticle(itemId)
        } else {
            throw IllegalStateException("You should set ITEM_ID_KEY in fragment attributes!")
        }

        closeButton.setOnClickListener {
            logger.debug("On click back")
            activity?.onBackPressed()
        }

        toggleButtonMute.setOnClickListener { _ ->
            viewModel.onClickToggleMute()
        }

        prevButton.setOnClickListener { _ ->
            viewModel.onClickPrev()
        }

        nextButton.setOnClickListener { _ ->
            viewModel.onClickNext()
        }

        val musicOnDrawable = ContextCompat.getDrawable(view.context, R.drawable.ic_music)!!
        val musicOffDrawable = ContextCompat.getDrawable(view.context, R.drawable.ic_music_off)!!

        viewModel.isMute.observe(viewLifecycleOwner, { isMute ->
            toggleButtonMute.setImageDrawable(if (isMute) musicOnDrawable else musicOffDrawable)
            mediaPlayer?.setMute(isMute)
        })

        articleCardView.visibility = View.INVISIBLE

        val controls = listOf(prevButton, nextButton)
        controls.forEach { btn -> btn.isVisible = false }

        viewModel.state.observe(viewLifecycleOwner, { readingState ->
            val articlePage = readingState.page
            logger.debug("Set new ${articlePage.articleId} article page")

            titleText.isVisible = titleText.text != null
            titleText.text = articlePage.title

            articleText.isVisible = articlePage.text != null
            articleText.text = articlePage.text

            articlePage.imageUrl?.let { imageUrl ->
                Glide.with(articleImage.context).load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(articleImage)
            }
            articleCardView.setVisibleWithTransition(
                View.VISIBLE,
                Slide(Gravity.END),
                1200,
                articlePageLayout,
                delayMs = 200
            )

            controls.forEach { btn ->
                btn.isVisible = true
                btn.runLevitateAnimation(4f, 700L)
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
                tracks.random().url
            }
            mediaPlayer?.playAudio(requireContext(), File(trackUrl))
        })

        mediaPlayer = BackgroundMediaPlayer()
    }

    override fun onResume() {
        super.onResume()
        logger.debug("onResume()")
        mediaPlayer?.let { player ->
            if (player.isPaused()) {
                logger.debug("Start paused playing")
                player.start()
            }
        }
    }

    override fun onPause() {
        mediaPlayer?.pause()
        logger.debug("onPause()")
        super.onPause()
    }

    override fun onDestroyView() {
        logger.debug("onDestroyView()")
        mediaPlayer?.stopAndRelease()
        mediaPlayer = null
        super.onDestroyView()
    }
}