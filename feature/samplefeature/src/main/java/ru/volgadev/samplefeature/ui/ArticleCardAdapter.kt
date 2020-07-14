package ru.volgadev.samplefeature.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AnyThread
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.card_article.view.*
import ru.volgadev.common.log.Logger
import ru.volgadev.sampledata.model.Article
import ru.volgadev.samplefeature.R

class ArticleCardAdapter :
    RecyclerView.Adapter<ArticleCardAdapter.ViewHolder>() {

    class ViewHolder(val card: CardView) : RecyclerView.ViewHolder(card)

    interface OnItemClickListener {
        fun onClick(itemId: Long)
    }
    @Volatile
    private var onItemClicklistener: OnItemClickListener? = null

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
    fun setOnItemClickListener(listener: OnItemClickListener){
        onItemClicklistener = listener
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articleList[position]
        val textView = holder.card.findViewById<TextView>(R.id.card_view_title)
        val image = holder.card.findViewById<ImageView>(R.id.card_view_image)
        Glide.with(image.context).load(article.iconUrl).into(image)
        textView.text = article.title
        holder.card.setOnClickListener {
            logger.debug("On click ${article.id}")
            onItemClicklistener?.onClick(article.id)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = articleList.size
}
