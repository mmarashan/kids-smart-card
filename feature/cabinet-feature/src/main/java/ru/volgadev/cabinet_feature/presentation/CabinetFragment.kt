package ru.volgadev.cabinet_feature.presentation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.cabinet_fragment.*
import ru.volgadev.cabinet_feature.BuildConfig
import ru.volgadev.cabinet_feature.R
import ru.volgadev.common.log.Logger
import ru.volgadev.common.observeOnce
import ru.volgadev.pincode_bubble.PinCodeBubbleAlertDialog
import ru.volgadev.pincode_bubble.quizgenerator.NumbersAdditionQuizGenerator

class CabinetFragment : Fragment(R.layout.cabinet_fragment) {

    private val logger = Logger.get("CabinetFragment")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("On fragment created; savedInstanceState=$savedInstanceState")

        val viewModel = ViewModelProvider(this, CabinetViewModelFactory).get(CabinetViewModel::class.java)

        val categoriesAdapter = CategoryCardAdapter().apply {
            setOnItemClickListener(object : CategoryCardAdapter.OnItemClickListener {
                override fun onClick(categoryName: String, clickedView: View) {
                    val clickedCategory =
                        viewModel.categories.value?.first { category -> category.name == categoryName }
                    clickedCategory?.let { category ->
                        logger.debug("On click category $category")
                        if (!category.isFree && (!category.isPaid || BuildConfig.DEBUG)) {
                            checkCorrectPayment { isCorrectAnswer ->
                                if (isCorrectAnswer) {
                                    viewModel.onReadyToPayment(category)
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
                    }
                }
            })
        }

        contentRecyclerView.run {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            overScrollMode = View.OVER_SCROLL_NEVER
            adapter = categoriesAdapter
        }

        viewModel.categories.observe(viewLifecycleOwner, { categories ->
            logger.debug("Set new market categories ${categories.joinToString(",")} ")
            categoriesAdapter.setData(categories)
        })
    }

    @MainThread
    private inline fun checkCorrectPayment(crossinline onAnswer: (Boolean) -> Unit) {
        logger.debug("checkCorrectPayment()")
        val activity = this@CabinetFragment.requireActivity()
        PinCodeBubbleAlertDialog.create(
            activity = activity,
            title = "Сначала ответьте на вопрос",
            NumbersAdditionQuizGenerator::class.java,
            hideNavigationBar = true
        ).showForResult().observeOnce { isCorrectAnswer ->
            onAnswer(isCorrectAnswer)
        }
    }

    override fun onResume() {
        super.onResume()
        logger.debug("onResume()")
    }

    override fun onPause() {
        logger.debug("onPause()")
        super.onPause()
    }

    override fun onDestroy() {
        logger.debug("onDestroy()")
        super.onDestroy()
    }
}