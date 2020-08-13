package ru.volgadev.article_galery.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.AnyThread
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ru.volgadev.article_galery.R

class ArticleTagsAdapter :
    RecyclerView.Adapter<ArticleTagsAdapter.ViewHolder>() {

    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card)

    private val tags = ArrayList<String>()

    @AnyThread
    fun setDataset(dataset: Collection<String>) {
        tags.clear()
        dataset.forEach { article ->
            tags.add(article)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val card = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_tag, parent, false) as CardView

        return ViewHolder(card)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = tags[position]
        val tagTextView = holder.card.findViewById<TextView>(R.id.tagTextView)
        tagTextView.text = tag
    }

    override fun getItemCount() = tags.size
}
