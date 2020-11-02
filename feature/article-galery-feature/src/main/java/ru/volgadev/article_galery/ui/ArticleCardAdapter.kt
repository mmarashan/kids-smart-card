package ru.volgadev.article_galery.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AnyThread
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_galery.R
import ru.volgadev.common.log.Logger
import ru.volgadev.common.runLevitateAnimation


class ArticleCardAdapter :
    RecyclerView.Adapter<ArticleCardAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onClick(itemId: Long, clickedView: View)
    }

    @Volatile
    private var onItemClickListener: OnItemClickListener? = null

    private val logger = Logger.get("ArticleCardAdapter")

    private var articleList = ArrayList<Article>()

    @AnyThread
    fun setData(dataset: Collection<Article>) {
        logger.debug("Set dataset with ${dataset.size} members")

        if (articleList.isNotEmpty()) {
            val length = articleList.size
            articleList.clear()
            notifyItemRangeRemoved(0, length);
        }
        articleList = ArrayList(dataset)
        notifyItemRangeInserted(0, articleList.size)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val card = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_article, parent, false) as CardView

        return ViewHolder(card, parent)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    inner class ViewHolder(val card: CardView, val parent: ViewGroup) :
        RecyclerView.ViewHolder(card) {

        private val viewClickListener = View.OnClickListener { view ->
            view?.let {
                val id = view.tag as Long
                logger.debug("On click $id")
                onItemClickListener?.onClick(id, view)
            }
        }

        private val cardArticleView: CardView = card.findViewById<CardView>(R.id.cardArticleView)
        private val author: TextView = card.findViewById<TextView>(R.id.cardAuthor)
        private val title: TextView = card.findViewById<TextView>(R.id.cardTitle)
        private val image: ImageView = card.findViewById<ImageView>(R.id.cardImage)

        private val tagsRecyclerView: RecyclerView =
            card.findViewById<RecyclerView>(R.id.cardTagsRecyclerView)
        private val tagsAdapter = TagsAdapter(R.layout.card_tag)

        init {
            tagsRecyclerView.run {
                setHasFixedSize(false)
                layoutManager =
                    LinearLayoutManager(
                        tagsRecyclerView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                adapter = tagsAdapter
                val dividerDrawable =
                    ContextCompat.getDrawable(context, R.drawable.empty_divider_4)!!
                val dividerDecorator =
                    DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL).apply {
                        setDrawable(dividerDrawable)
                    }
                addItemDecoration(dividerDecorator)
            }
            image.runLevitateAnimation(4f, 700L)
        }

        fun bind(article: Article) {
            val holder = this
            card.tag = article.id
            val image = holder.image
            holder.title.text = article.title

            if (article.title.isNotEmpty()) {
                holder.title.isVisible = true
                holder.title.text = article.title
            } else {
                holder.title.isVisible = false
            }

            if (article.author.isNotEmpty()) {
                holder.author.isVisible = true
                holder.author.text = article.author
            } else {
                holder.author.isVisible = false
            }

            if (article.tags.isNotEmpty()) {
                holder.tagsRecyclerView.isVisible = true
                holder.tagsAdapter.setData(article.tags)
            } else {
                holder.tagsRecyclerView.isVisible = false
            }
            article.iconUrl?.let { url ->
                Glide.with(image.context).load(url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(image)
            }

            holder.card.setOnClickListener(viewClickListener)

            logger.debug("Bind card elevation = ${card.elevation} ${card.cardElevation}")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articleList[position]
        holder.bind(article)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = articleList.size
}
