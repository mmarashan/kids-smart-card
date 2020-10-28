package ru.volgadev.cabinet_feature

import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.annotation.AnyThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.cabinet_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.common.BackgroundMediaPlayer
import ru.volgadev.common.log.Logger

class CabinetFragment : Fragment(R.layout.cabinet_fragment) {

    private val logger = Logger.get("categoryGalleryFragment")

    companion object {
        fun newInstance() = CabinetFragment()
    }

    private val viewModel: CabinetViewModel by viewModel()

    private val musicMediaPlayer by lazy { BackgroundMediaPlayer() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("On fragment created; savedInstanceState=$savedInstanceState")

        val categoriesAdapter = CategoryCardAdapter().apply {
            setOnItemClickListener(object : CategoryCardAdapter.OnItemClickListener {
                override fun onClick(categoryName: String, clickedView: View) {
                    val clickedCategory =
                        viewModel.marketCategories.value?.first { category -> category.category.name == categoryName }
                    clickedCategory?.let { category ->
                        logger.debug("On click category $category")
                        viewModel.onClickCategory(category.category)
                    }
                }
            })
        }


        contentRecyclerView.run {
            layoutManager = LinearLayoutManager(
                this.context,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = categoriesAdapter
            itemAnimator = LandingAnimator(OvershootInterpolator(1f)).apply {
                addDuration = 700
                removeDuration = 100
                moveDuration = 700
                changeDuration = 100
            }
        }

        viewModel.marketCategories.observe(viewLifecycleOwner, Observer { categories ->
            logger.debug("Set new ${categories.size} categories")
            categoriesAdapter.setData(categories)
        })
    }

    override fun onResume() {
        super.onResume()
        logger.debug("onResume()")
        if (musicMediaPlayer.isPaused()) {
            logger.debug("Start paused playing")
            musicMediaPlayer.start()
        }
    }

    override fun onPause() {
        logger.debug("onPause()")
        musicMediaPlayer.pause()
        super.onPause()
    }
}