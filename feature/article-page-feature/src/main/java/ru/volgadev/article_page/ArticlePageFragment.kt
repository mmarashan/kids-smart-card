package ru.volgadev.article_page

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.jackandphantom.customtogglebutton.CustomToggle
import kotlinx.android.synthetic.main.layout_article_page.*
import kotlinx.android.synthetic.main.layout_bottom_controls.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.common.log.Logger
import ru.volgadev.common.playAudio


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

        buttonMute.setOnClickListener { btn ->
            btn.postDelayed({
                viewModel.onClickToggleMute()
            }, 100)
        }

        buttonAutoScroll.setOnClickListener { btn ->
            btn.postDelayed({
                viewModel.onClickToggleAutoScroll()
            }, 100)
        }

        // TODO: избавиться от мигания
        viewModel.isMute.observe(viewLifecycleOwner, Observer { isMute ->
            buttonMute.isPressed = isMute
        })

        viewModel.isAutoScroll.observe(viewLifecycleOwner, Observer { isAutoScroll ->
            buttonAutoScroll.isPressed = isAutoScroll
        })

        // TODO: разобраться с кастомным тоглом
        customToggle.setOnToggleClickListener(object : CustomToggle.OnToggleClickListener {
            override fun onLefToggleEnabled(enabled: Boolean) {
                logger.debug("onLefToggleEnabled")
            }

            override fun onRightToggleEnabled(enabled: Boolean) {
                logger.debug("onRightToggleEnabled")
            }
        })

        viewModel.article.observe(viewLifecycleOwner, Observer { article ->
            logger.debug("Set new ${article.id} article")
            toolbarText.text = article.title
            articleText.text = article.text
            if (article.iconUrl != null) Glide.with(articleImage.context).load(article.iconUrl)
                .into(articleImage)
            viewLifecycleOwner.lifecycleScope.launch {
                playAudio("https://raw.githubusercontent.com/mmarashan/psdata/master/audio/1.mp3")
            }
        })
    }

    private suspend fun playAudio(path: String) = withContext(Dispatchers.Default) {
        context?.applicationContext?.let { appContext ->
            logger.debug("Play $path")
            mediaPlayer.playAudio(appContext, path)
        }
    }
}