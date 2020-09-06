package ru.volgadev.article_galery.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.annotation.AnyThread
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
import ru.volgadev.article_galery.R
import ru.volgadev.common.log.Logger
import ru.volgadev.common.playAudio


class ArticleGalleryFragment : Fragment(R.layout.main_fragment) {

    private val logger = Logger.get("ArticleGalleryFragment")

    companion object {
        fun newInstance() = ArticleGalleryFragment()
    }

    private val viewModel: ArticleGalleryViewModel by viewModel()

    private val mediaPlayer: MediaPlayer = MediaPlayer()

    interface OnItemClickListener {
        fun onClick(itemId: Long, clickedView: View)
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
                    onItemClickListener?.onClick(itemId, clickedView)
                }
            })
        }

        contentRecyclerView.run {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = viewAdapter
        }

        viewModel.articles.observe(viewLifecycleOwner, Observer { articles ->
            logger.debug("Set new ${articles.size} articles")
            viewAdapter.setData(articles)
        })

        viewModel.tracks.observe(viewLifecycleOwner, Observer { tracks ->
            logger.debug("On new ${tracks.size} articles")
            val trackUrl = tracks[0].url
            viewLifecycleOwner.lifecycleScope.launch {
                playAudio(trackUrl)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        logger.debug("onResume()")
        mediaPlayer.start()
    }

    override fun onPause() {
        mediaPlayer.pause()
        logger.debug("onPause()")
        super.onPause()
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

}