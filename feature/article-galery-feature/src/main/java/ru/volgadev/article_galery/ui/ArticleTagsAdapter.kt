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

    private val tags = ArrayList<String>()

    @AnyThread
    fun setData(dataset: Collection<String>) {
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

    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card) {
        val tagTextView: TextView = card.findViewById<TextView>(R.id.tagTextView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tagTextView.text = tags[position]
    }

    override fun getItemCount() = tags.size
}
