package ru.volgadev.article_galery.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AnyThread
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import ru.volgadev.article_galery.R
import ru.volgadev.article_repository.domain.model.Article

internal class ArticleCardAdapter :
    RecyclerView.Adapter<ViewHolder>() {

    interface OnItemClickListener {
        fun onClick(item: Article, clickedView: View)
    }

    @Volatile
    private var onItemClickListener: OnItemClickListener? = null

    private var articleList = ArrayList<Article>()

    @AnyThread
    fun setData(dataset: Collection<Article>) {
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
        return ViewHolder(card)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(position, articleList[position], onItemClickListener)

    override fun getItemCount() = articleList.size
}

internal class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card) {

    private val author = card.findViewById<TextView>(R.id.cardAuthor)
    private val title = card.findViewById<TextView>(R.id.cardTitle)
    private val image = card.findViewById<ImageView>(R.id.cardImage)

    private var currentPosition = 0

    fun bind(
        position: Int,
        article: Article,
        onItemClickListener: ArticleCardAdapter.OnItemClickListener?
    ) {
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

        article.iconUrl?.let { url ->
            Glide.with(image.context).load(url)
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(image)
        }

        holder.card.setOnClickListener {
            onItemClickListener?.onClick(article, it)
        }
    }
}
