package ru.volgadev.article_page

import android.media.MediaPlayer
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.layout_article_page.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.common.log.Logger
import ru.volgadev.common.mute
import ru.volgadev.common.playAudio
import ru.volgadev.common.runLevitateAnimation
import ru.volgadev.common.setVisibleWithTransition

const val ITEM_ID_KEY = "ITEM_ID"

class ArticlePageFragment : Fragment(R.layout.layout_article_page) {

    private val logger = Logger.get("ArticlePageFragment")

    companion object {
        fun newInstance() = ArticlePageFragment()
    }

    private val viewModel: ArticlePageViewModel by viewModel()

    private var mediaPlayer: MediaPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("On fragment created")

        val musicOnDrawable = ContextCompat.getDrawable(view.context, R.drawable.ic_music)!!
        val musicOffDrawable = ContextCompat.getDrawable(view.context, R.drawable.ic_music_off)!!

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

        viewModel.isMute.observe(viewLifecycleOwner, Observer { isMute ->
            toggleButtonMute.setImageDrawable(if (isMute) musicOnDrawable else musicOffDrawable)
            mediaPlayer?.mute(isMute)
        })

        articleCardView.visibility = View.INVISIBLE

        val controls = listOf(prevButton, nextButton)
        controls.forEach { btn -> btn.isVisible = false }

        viewModel.state.observe(viewLifecycleOwner, Observer { readingState ->
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

        viewModel.tracks.observe(viewLifecycleOwner, Observer { tracks ->
            logger.debug("On new ${tracks.size} tracks")
            val downloadedTracks = tracks.filter { track -> track.filePath != null }
            val trackUrl = if (downloadedTracks.isNotEmpty()) {
                logger.debug("Play downloaded")
                downloadedTracks.random().filePath!!
            } else {
                logger.debug("Play from streaming")
                tracks.random().url
            }
            viewLifecycleOwner.lifecycleScope.launch {
                playAudio(trackUrl)
            }
            viewLifecycleOwner.lifecycleScope.launch {
                playAudio(trackUrl)
            }
        })

        mediaPlayer = MediaPlayer()
    }

    override fun onResume() {
        super.onResume()
        logger.debug("onResume()")
        mediaPlayer?.start()
    }

    override fun onPause() {
        mediaPlayer?.pause()
        logger.debug("onPause()")
        super.onPause()
    }

    override fun onDestroyView() {
        logger.debug("onDestroyView()")
        mediaPlayer?.let { player ->
            player.stop()
            player.release()
        }
        mediaPlayer = null
        super.onDestroyView()
    }

    private suspend fun playAudio(path: String) = withContext(Dispatchers.Default) {
        context?.applicationContext?.let { appContext ->
            logger.debug("Play $path")
            mediaPlayer?.playAudio(appContext, path)
        }
    }
}