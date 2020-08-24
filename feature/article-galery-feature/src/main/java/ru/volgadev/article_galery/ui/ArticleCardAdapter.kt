package ru.volgadev.article_galery.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AnyThread
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_galery.R
import ru.volgadev.common.log.Logger


class ArticleCardAdapter :
    RecyclerView.Adapter<ArticleCardAdapter.ViewHolder>() {

    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card)

    interface OnItemClickListener {
        fun onClick(itemId: Long, clickedView: View)
    }

    @Volatile
    private var onItemClickListener: OnItemClickListener? = null

    private val logger = Logger.get("ArticleCardAdapter")

    private val articleList = ArrayList<Article>()

    @AnyThread
    fun setDataset(dataset: Collection<Article>) {
        logger.debug("Set dataset with ${dataset.size} members")
        articleList.clear()
        dataset.forEach { article ->
            articleList.add(article)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val card = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_article, parent, false) as CardView

        return ViewHolder(card)
    }

    @AnyThread
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articleList[position]
        val author = holder.card.findViewById<TextView>(R.id.cardAuthor)
        val title = holder.card.findViewById<TextView>(R.id.cardTitle)
        val image = holder.card.findViewById<ImageView>(R.id.cardImage)

        val tagsRecyclerView = holder.card.findViewById<RecyclerView>(R.id.cardTagsRecyclerView)
        tagsRecyclerView.run {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(tagsRecyclerView.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ArticleTagsAdapter().apply {
                setDataset(article.tags)
            }
            val dividerDrawable = ContextCompat.getDrawable(context, R.drawable.empty_divider_4)!!
            val dividerDecorator =
                DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL).apply {
                    setDrawable(dividerDrawable)
                }
            addItemDecoration(dividerDecorator)
        }

        article.iconUrl ?: Glide.with(image.context).load(article.iconUrl).into(image)
        author.text = article.author
        title.text = article.title

        holder.card.setOnClickListener { card ->
            logger.debug("On click ${article.id}")
            onItemClickListener?.onClick(article.id, card)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = articleList.size
}
