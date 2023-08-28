package ru.volgadev.cabinet_feature.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.cabinet_feature.R
import ru.volgadev.cabinet_feature.databinding.CabinetFragmentBinding
import ru.volgadev.common.BuildConfig
import ru.volgadev.common.logger.Logger
import ru.volgadev.pincode_bubble.PinCodeBubbleAlertDialog
import ru.volgadev.pincode_bubble.quizgenerator.impl.NumbersAdditionQuizGenerator

class CabinetFragment : Fragment(R.layout.cabinet_fragment) {

    private val logger = Logger.get("CabinetFragment")

    private val viewModel: CabinetViewModel by viewModel()

    private val numbersAdditionQuizGenerator by lazy { NumbersAdditionQuizGenerator(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = CabinetFragmentBinding.inflate(inflater, container, false)

        val categoriesAdapter = CategoryCardAdapter().apply {
            setOnItemClickListener(object : CategoryCardAdapter.OnItemClickListener {
                override fun onClick(categoryName: String, clickedView: View) {
                    val clickedCategory =
                        viewModel.categories.value?.first { category -> category.name == categoryName }
                    // TODO: refactor it. move logic to vm and interactor
                    clickedCategory?.let { category ->
                        logger.debug("On click category $category")
                        if (!category.isFree && (!category.isPaid || BuildConfig.DEBUG)) {
                            checkIsClickedByAdult { isClickedByAdult ->
                                if (isClickedByAdult) {
                                    viewModel.onReadyToPayment(category)
                                } else {
                                    logger.debug("incorrect answer")
                                    showFalseAnswer()
                                }
                            }
                        }
                    }
                }
            })
        }

        binding.contentRecyclerView.run {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            overScrollMode = View.OVER_SCROLL_NEVER
            adapter = categoriesAdapter
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            logger.debug("Set new market categories ${categories.joinToString(",")} ")
            categoriesAdapter.setData(categories)
        }

        return binding.root
    }

    private inline fun checkIsClickedByAdult(crossinline onAnswer: (Boolean) -> Unit) {
        logger.debug("checkIsClickedByAdult()")
        val activity = this@CabinetFragment.requireActivity()
        this@CabinetFragment.lifecycleScope.launchWhenResumed {
            PinCodeBubbleAlertDialog.create(
                activity = activity,
                title = getString(R.string.quiz_alert_title),
                numbersAdditionQuizGenerator,
                hideNavigationBar = true
            ).showForResult().collect { isCorrectAnswer ->
                onAnswer(isCorrectAnswer)
            }
        }
    }

    private fun showFalseAnswer() = Toast.makeText(
        activity,
        getString(R.string.false_answer),
        Toast.LENGTH_SHORT
    ).show()
}