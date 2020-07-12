package ru.volgadev.samplefeature.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.main_fragment.*
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

        val gridLayoutManager = GridLayoutManager(context, 2)
        val viewAdapter = ArticleCardAdapter().apply {
            setOnItemClickListener(object : ArticleCardAdapter.OnItemClickListener {
                override fun onClick(itemId: Int) {
                    Toast.makeText(context, "On click $itemId", Toast.LENGTH_LONG).show()
                }
            })
        }

        contentRecyclerView.run {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            layoutManager = gridLayoutManager
            adapter = viewAdapter
        }

        viewModel.articles.observe(viewLifecycleOwner, Observer { articles ->
            logger.debug("Set new ${articles.size} articles")
            viewAdapter.setDataset(articles)
        })
    }

}