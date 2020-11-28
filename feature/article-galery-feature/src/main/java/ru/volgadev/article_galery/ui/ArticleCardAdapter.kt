package ru.volgadev.article_galery.ui

import android.content.Context
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
import jp.wasabeef.recyclerview.animators.OvershootInLeftAnimator
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_galery.R
import ru.volgadev.common.log.Logger
import ru.volgadev.common.runLevitateAnimation


class ArticleCardAdapter(val context: Context) :
    RecyclerView.Adapter<ArticleCardAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onClick(itemId: Long, clickedView: View, position: Int)
    }

    @Volatile
    private var onItemClickListener: OnItemClickListener? = null

    private val logger = Logger.get("ArticleCardAdapter")

    private var articleList = ArrayList<Article>()

    private val layoutInflater by lazy { LayoutInflater.from(context) }

    private val dividerDrawable4 by lazy {
        ContextCompat.getDrawable(context, R.drawable.empty_divider_4)!!
    }

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
        val card = layoutInflater.inflate(R.layout.card_article, parent, false) as CardView
        return ViewHolder(card)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    inner class ViewHolder(val card: CardView) :
        RecyclerView.ViewHolder(card), View.OnClickListener {

        private val author = card.findViewById<TextView>(R.id.cardAuthor)
        private val title = card.findViewById<TextView>(R.id.cardTitle)
        private val image = card.findViewById<ImageView>(R.id.cardImage)

        private val tagsRecyclerView =
            card.findViewById<RecyclerView>(R.id.cardTagsRecyclerView)
        private val tagsAdapter = TagsAdapter(R.layout.card_tag)

        private var currentPosition = 0

        init {
            tagsRecyclerView.run {
                setHasFixedSize(false)
                layoutManager =
                    LinearLayoutManager(
                        tagsRecyclerView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                itemAnimator = null
                adapter = tagsAdapter
                val dividerDecorator =
                    DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL).apply {
                        setDrawable(dividerDrawable4)
                    }
                addItemDecoration(dividerDecorator)
            }
        }

        fun bind(position: Int, article: Article) {
            val holder = this
            holder.currentPosition = position
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
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(image)
            }

            holder.card.setOnClickListener(this)

            logger.debug("Bind card elevation = ${card.elevation} ${card.cardElevation}")
        }

        override fun onClick(view: View?) {
            view?.let {
                val id = view.tag as Long
                logger.debug("On click $id")
                onItemClickListener?.onClick(id, view, currentPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articleList[position]
        holder.bind(position, article)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = articleList.size
}
