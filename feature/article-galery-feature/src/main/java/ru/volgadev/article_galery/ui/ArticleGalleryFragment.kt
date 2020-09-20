package ru.volgadev.article_galery.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.model.ArticleType
import ru.volgadev.article_galery.R
import ru.volgadev.common.BackgroundMediaPlayer
import ru.volgadev.common.log.Logger
import ru.volgadev.common.playAudio


class ArticleGalleryFragment : Fragment(R.layout.main_fragment) {

    private val logger = Logger.get("ArticleGalleryFragment")

    companion object {
        fun newInstance() = ArticleGalleryFragment()
    }

    private val viewModel: ArticleGalleryViewModel by viewModel()

    private var mediaPlayer: BackgroundMediaPlayer? = null

    interface OnItemClickListener {
        fun onClick(article: Article, clickedView: View)
    }

    @Volatile
    private var onItemClickListener: OnItemClickListener? = null

    @AnyThread
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("On fragment created; savedInstanceState=$savedInstanceState")

        val viewAdapter = ArticleCardAdapter().apply {
            setOnItemClickListener(object : ArticleCardAdapter.OnItemClickListener {
                override fun onClick(itemId: Long, clickedView: View) {
                    val clickedArticle =
                        viewModel.articles.value?.first { article -> article.id == itemId }
                    clickedArticle?.let { article ->
                        logger.debug("On click article ${article.id}")
                        article.onClickSounds.firstOrNull()?.let { firstSoundUrl ->
                            mediaPlayer?.playAudio(context!!, firstSoundUrl)
                        }
                        if (article.type == ArticleType.NO_PAGES) {

                        }
                        onItemClickListener?.onClick(article, clickedView)
                    }
                }
            })
        }

        contentRecyclerView.run {
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = viewAdapter
        }

        viewModel.articles.observe(viewLifecycleOwner, Observer { articles ->
            logger.debug("Set new ${articles.size} articles")
            viewAdapter.setData(articles)
        })

        viewModel.tracks.observe(viewLifecycleOwner, Observer { tracks ->
            logger.debug("On new ${tracks.size} tracks")
            val downloadedTracks = tracks.filter { track -> track.filePath != null }
            val trackUrl = if (downloadedTracks.isNotEmpty()) {
                logger.debug("Play downloaded")
                downloadedTracks.random().filePath!!
            } else {
                logger.debug("Play from streaming")
                if (tracks.isNotEmpty()) {
                    tracks.random().url
                } else {
                    null
                }
            }
            trackUrl?.let { url ->
                mediaPlayer?.playAudio(context!!, url)
            }
        })

        mediaPlayer = BackgroundMediaPlayer()
    }

    override fun onResume() {
        super.onResume()
        logger.debug("onResume()")
        mediaPlayer?.start()
    }

    override fun onPause() {
        logger.debug("onPause()")
        mediaPlayer?.pause()
        super.onPause()
    }

    override fun onDestroyView() {
        logger.debug("onDestroyView()")
        mediaPlayer?.stopAndRelease()
        mediaPlayer = null
        super.onDestroyView()
    }
}