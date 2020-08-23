package ru.volgadev.article_page

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_article_page.*
import kotlinx.android.synthetic.main.layout_bottom_controls.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.common.log.Logger
import ru.volgadev.common.mute
import ru.volgadev.common.playAudio
import kotlin.math.abs
import kotlin.math.roundToInt


const val ITEM_ID_KEY = "ITEM_ID"

class ArticlePageFragment : Fragment(R.layout.layout_article_page) {

    private val logger = Logger.get("ArticlePageFragment")

    companion object {
        fun newInstance() = ArticlePageFragment()
    }

    private val viewModel: ArticlePageViewModel by viewModel()

    private val mediaPlayer: MediaPlayer = MediaPlayer()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("On fragment created")

        val args = arguments
        if (args != null && args.containsKey(ITEM_ID_KEY)) {
            val itemId = args.getLong(ITEM_ID_KEY)
            viewModel.onChooseArticle(itemId)
        } else {
            throw IllegalStateException("You should set ITEM_ID_KEY in fragment attributes!")
        }

        backButton.setOnClickListener {
            logger.debug("On click back")
            activity?.onBackPressed()
        }

        toggleButtonMute.setOnClickListener { btn ->
            btn.postDelayed({
                viewModel.onClickToggleMute()
            }, 100)
        }

        toggleAutoScroll.setOnClickListener { btn ->
            btn.postDelayed({
                viewModel.onClickToggleAutoScroll()
            }, 100)
        }

        viewModel.isMute.observe(viewLifecycleOwner, Observer { isMute ->
            toggleButtonMute.isPressed = isMute
            mediaPlayer.mute(isMute)
        })

        var scrollProgressPix = 0
        val deltaThresholdPix = 40
        val scrollStepDurationMs = 1000
        val scrollVelocityPixPerSec =
            (1f * deltaThresholdPix / (1f * scrollStepDurationMs / 1000)).roundToInt()

        viewModel.isAutoScroll.observe(viewLifecycleOwner, Observer { isAutoScroll ->
            toggleAutoScroll.isPressed = isAutoScroll
            if (isAutoScroll) {
                articleTextNestedScrollView.scrollDown(
                    scrollVelocityPixPerSec,
                    scrollStepDurationMs
                )
            }
        })

        articleText.setOnClickListener {
            viewModel.onClickText()
        }

        articleTextNestedScrollView.setOnScrollChangeListener { _: NestedScrollView?,
                                                                _: Int, scrollY: Int,
                                                                _: Int, oldScrollY: Int ->
            val isAutoScroll = viewModel.isAutoScroll.value ?: false
            val isScrollDown = scrollY > scrollProgressPix
            val delta = abs(scrollY - scrollProgressPix)
            if (delta >= deltaThresholdPix) {
                scrollProgressPix = scrollY
                logger.debug("OnScrollChange scrollY=$scrollY, oldScrollY=$oldScrollY")
                if (isAutoScroll && isScrollDown) articleTextNestedScrollView.scrollDown(
                    scrollVelocityPixPerSec,
                    scrollStepDurationMs
                )
            }
        }

        viewModel.article.observe(viewLifecycleOwner, Observer { article ->
            logger.debug("Set new ${article.id} article")
            val title = "${article.title}. ${article.author}"
            titleText.text = title
            articleText.text = article.text
            if (article.iconUrl != null) Glide.with(articleImage.context).load(article.iconUrl)
                .into(articleImage)
            viewLifecycleOwner.lifecycleScope.launch {
                playAudio("https://raw.githubusercontent.com/mmarashan/psdata/master/audio/1.mp3")
            }
        })
    }

    override fun onDestroyView() {
        logger.debug("onDestroyView()")
        super.onDestroyView()
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    private suspend fun playAudio(path: String) = withContext(Dispatchers.Default) {
        context?.applicationContext?.let { appContext ->
            logger.debug("Play $path")
            mediaPlayer.playAudio(appContext, path)
        }
    }

    private fun NestedScrollView.scrollDown(velocityPixPerSec: Int, durationMs: Int) {
        val dy = velocityPixPerSec * durationMs / 1000
        smoothScrollBy(0, dy, durationMs)
    }
}