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
import java.util.*


const val ITEM_ID_KEY = "ITEM_ID"
private const val SCROLL_STEP_DURATION_MS = 1000
private const val SCROLL_VELOCITY_PIX_PER_SEC = 70

class ArticlePageFragment : Fragment(R.layout.layout_article_page) {

    private val logger = Logger.get("ArticlePageFragment")

    companion object {
        fun newInstance() = ArticlePageFragment()
    }

    private val viewModel: ArticlePageViewModel by viewModel()

    private val mediaPlayer: MediaPlayer = MediaPlayer()

    private val autoScrollTimer = Timer()

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
                viewModel.onToggleAutoScroll()
            }, 100)
        }

        viewModel.isMute.observe(viewLifecycleOwner, Observer { isMute ->
            toggleButtonMute.isPressed = isMute
            mediaPlayer.mute(isMute)
        })

        viewModel.isAutoScroll.observe(viewLifecycleOwner, Observer { isAutoScroll ->
            logger.debug("isAutoScroll=$isAutoScroll")
            toggleAutoScroll.isPressed = isAutoScroll
            if (isAutoScroll) {
                articleTextNestedScrollView.scrollDown(
                    SCROLL_VELOCITY_PIX_PER_SEC,
                    SCROLL_STEP_DURATION_MS
                )
            }
        })

        articleTextNestedScrollView.setOnScrollChangeListener { _: NestedScrollView?,
                                                                _: Int, scrollY: Int,
                                                                _: Int, _: Int ->
            if (articleText.height != 0) {
                viewModel.onScrollProgress(1f * scrollY / articleText.height)
            }
        }

        autoScrollTimer.schedule(object : TimerTask() {
            override fun run() {
                val isAutoScroll = viewModel.isAutoScroll.value ?: false
                if (isAutoScroll) {
                    articleTextNestedScrollView?.scrollDown(
                        SCROLL_VELOCITY_PIX_PER_SEC,
                        SCROLL_STEP_DURATION_MS
                    )
                }
            }
        }, 0, SCROLL_STEP_DURATION_MS * 1L)

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
        autoScrollTimer.cancel()
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