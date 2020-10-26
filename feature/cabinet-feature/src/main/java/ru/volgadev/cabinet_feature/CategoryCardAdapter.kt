package ru.volgadev.cabinet_feature

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AnyThread
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.common.log.Logger

class CategoryCardAdapter :
    RecyclerView.Adapter<CategoryCardAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onClick(categoryName: String, clickedView: View)
    }

    @Volatile
    private var onItemClickListener: OnItemClickListener? = null

    private val logger = Logger.get("ArticleCategoryCardAdapter")

    private val categoryList = ArrayList<ArticleCategory>()

    @AnyThread
    fun setData(dataset: Collection<ArticleCategory>) {
        logger.debug("Set dataset with ${dataset.size} members")

        if (categoryList.isNotEmpty()) {
            val length = categoryList.size
            categoryList.clear()
            (0..length).iterator().forEach { index ->
                notifyItemRemoved(index)
            }
        }
        dataset.forEachIndexed { index, article ->
            categoryList.add(article)
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val card = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_category, parent, false) as CardView

        return ViewHolder(card, parent)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    inner class ViewHolder(val card: CardView, val parent: ViewGroup) :
        RecyclerView.ViewHolder(card) {

        private val viewClickListener = View.OnClickListener { view ->
            view?.let {
                val id = view.tag as String
                logger.debug("On click $id")
                onItemClickListener?.onClick(id, view)
            }
        }

        private val cardArticleCategoryView: CardView =
            card.findViewById(R.id.categoryCardView)
        private val starStatus: ImageView = card.findViewById(R.id.categoryStatus)
        private val title: TextView = card.findViewById(R.id.categoryTitle)
        private val image: ImageView = card.findViewById(R.id.categoryImage)
        private val description: TextView = card.findViewById(R.id.categoryDescription)

        fun bind(category: ArticleCategory) {
            val holder = this
            card.tag = category.name
            val image = holder.image
            holder.title.text = category.name
            holder.description.text = category.description

            category.iconUrl?.let { url ->
                Glide.with(image.context).load(url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(image)
            }

            holder.card.setOnClickListener(viewClickListener)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = categoryList[position]
        holder.bind(article)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = categoryList.size
}
