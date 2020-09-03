package ru.volgadev.article_page

// import kotlinx.android.synthetic.main.layout_article_page.*
import android.media.MediaPlayer
import android.os.Bundle
import android.transition.*
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_article_page.*
import kotlinx.android.synthetic.main.layout_article_page.view.*
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

private const val ROWS_PER_SEC = 1
private const val SCROLL_FPS = 60

class ArticlePageFragment : Fragment(R.layout.layout_article_page) {

    private val logger = Logger.get("ArticlePageFragment")

    companion object {
        fun newInstance() = ArticlePageFragment()
    }

    private val viewModel: ArticlePageViewModel by viewModel()

    private val mediaPlayer: MediaPlayer = MediaPlayer()

    private var autoScrollTimer: Timer = Timer()
    private var pixelDownPerStep = 0
    private val scrollStepMs = 1000 / SCROLL_FPS

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
                pixelDownPerStep = articleText.lineHeight * ROWS_PER_SEC / SCROLL_FPS
                articleNestedScrollView?.smoothScrollBy(
                    0,
                    pixelDownPerStep,
                    scrollStepMs
                )
            }
        })

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

        articleNestedScrollView.setOnScrollChangeListener { _: NestedScrollView?,
                                                            _: Int, scrollY: Int,
                                                            _: Int, _: Int ->
            // @WARNING this callback can be called from background thread!!!
            if (articleText != null && articleText.height != 0) {
                viewModel.onScrollProgress(1f * scrollY / articleText.height)
            }
        }

        startButton.setOnClickListener { startBtn ->
            logger.debug("on click startButton")
            viewModel.onClickStart()
        }

        viewModel.isStarted.observe(viewLifecycleOwner, Observer { isStarted ->
            if (isStarted) {
                bottomControls.setVisibleWithTransition(View.VISIBLE, Explode(), 3000, articlePageLayout)
                // TODO: remove magic constants
                // TODO: hide custom navbar
                startButton.setVisibleWithTransition(View.INVISIBLE, Fade(), 3000, articlePageLayout)
                articleNestedScrollView.setScrollable(true)
                articleText.postDelayed({
                    viewModel.onToggleAutoScroll(true)
                }, 1000L)
            } else {
                startButton.visibility = View.VISIBLE
                bottomControls.visibility = View.GONE
                articleNestedScrollView.setScrollable(false)
                viewModel.onToggleAutoScroll(false)
            }
        })
    }

    private fun createAutoScrollTimerWithTask(): Timer {
        logger.debug("runTimerTask()")
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val isAutoScroll = viewModel.isAutoScroll.value ?: false
                if (isAutoScroll) {
                    articleNestedScrollView?.post {
                        articleNestedScrollView?.smoothScrollBy(
                            0,
                            pixelDownPerStep,
                            scrollStepMs
                        )
                    }
                }
            }
        }, 0, scrollStepMs * 1L)
        return timer
    }

    override fun onResume() {
        super.onResume()
        logger.debug("onResume()")
        autoScrollTimer = createAutoScrollTimerWithTask()
        mediaPlayer.start()
    }

    override fun onPause() {
        autoScrollTimer.purge()
        autoScrollTimer.cancel()
        mediaPlayer.pause()
        logger.debug("onPause()")
        super.onPause()
    }

    override fun onDestroyView() {
        logger.debug("onDestroyView()")
        super.onDestroyView()
        mediaPlayer.stop()
        mediaPlayer.release()
        autoScrollTimer.purge()
        autoScrollTimer.cancel()
    }

    private suspend fun playAudio(path: String) = withContext(Dispatchers.Default) {
        context?.applicationContext?.let { appContext ->
            logger.debug("Play $path")
            mediaPlayer.playAudio(appContext, path)
        }
    }

    private fun View.setVisibleWithTransition(
        visibility: Int,
        transition: Transition,
        durationMs: Long,
        parent: ViewGroup
    ) {
        transition.duration = durationMs
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(parent, transition)
        this.visibility = visibility
    }

    private fun NestedScrollView.setScrollable(scrollable: Boolean) {
        setOnTouchListener { _, _ -> !scrollable }
    }
}