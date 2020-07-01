package ru.volgadev.samplefeature.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.common.log.Logger
import ru.volgadev.samplefeature.R

class SampleFragment : Fragment(R.layout.main_fragment) {

    private val logger = Logger.get("SampleFragment")

    companion object {
        fun newInstance() = SampleFragment()
    }

    private val viewModel: SampleViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("On fragment created")

        val message: TextView = view.findViewById(R.id.message)

        viewModel.articles.observe(viewLifecycleOwner, Observer { articles ->
            val resultString = StringBuilder()
            articles.forEach { article ->
                resultString.append(article.id.toString()).append(" ").append(article.title)
                    .append("\n")
            }
            message.text = resultString.toString()

        })
    }

}