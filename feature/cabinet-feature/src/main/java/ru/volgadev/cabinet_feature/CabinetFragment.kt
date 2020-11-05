package ru.volgadev.cabinet_feature

import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.cabinet_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.common.BackgroundMediaPlayer
import ru.volgadev.common.log.Logger
import ru.volgadev.common.observeOnce
import ru.volgadev.pincode_bubble.PinCodeBubbleAlertDialog

class CabinetFragment : Fragment(R.layout.cabinet_fragment) {

    private val logger = Logger.get("CabinetFragment")

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
                        viewModel.categories.value?.first { category -> category.name == categoryName }
                    clickedCategory?.let { category ->
                        logger.debug("On click category $category")
                        if (!category.isFree && !category.isPaid) {
                            checkCorrectPayment(category)
                        }
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

        viewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            logger.debug("Set new market categories ${categories.joinToString(",")} ")
            categoriesAdapter.setData(categories)
        })
    }

    @MainThread
    private fun checkCorrectPayment(marketCategory: ArticleCategory){
        logger.debug("checkCorrectPayment()")
        val activity = this@CabinetFragment.requireActivity()
        PinCodeBubbleAlertDialog(
            activity = activity,
            title = "Сначала ответьте на вопрос",
            question = "Дважды два",
            answers = listOf("4", "четыре", "Четыре"),
            hideNavigationBar = true
        ).showForResult().observeOnce { isCorrectAnswer ->
            if (isCorrectAnswer) {
                logger.debug("correct answer")
                viewModel.onReadyToPayment(marketCategory)
            } else {
                logger.debug("incorrect answer")
                Toast.makeText(
                    activity,
                    getString(R.string.false_answer),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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