package ru.volgadev.cardgallery.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import ru.volgadev.cardgallery.databinding.CardArticleBinding
import ru.volgadev.cardrepository.domain.model.Card

internal class ArticleCardAdapter : RecyclerView.Adapter<ViewHolder>() {

    interface OnItemClickListener {
        fun onClick(item: Card, clickedView: View, position: Int)
    }

    @Volatile
    private var onItemClickListener: OnItemClickListener? = null

    private var articleList = ArrayList<Card>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = CardArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(position, articleList[position], onItemClickListener)

    override fun getItemCount() = articleList.size

    fun setData(dataset: Collection<Card>) {
        if (articleList.isNotEmpty()) {
            val length = articleList.size
            articleList.clear()
            notifyItemRangeRemoved(0, length)
        }
        articleList = ArrayList(dataset)
        notifyItemRangeInserted(0, articleList.size)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }
}

internal class ViewHolder(private val binding: CardArticleBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        position: Int,
        card: Card,
        onItemClickListener: ArticleCardAdapter.OnItemClickListener?
    ) {
        binding.cardTitle.text = card.title

        binding.cardTitle.isVisible = card.title.isNotEmpty()
        binding.cardTitle.text = card.title

        binding.cardAuthor.isVisible = card.author.isNotEmpty()
        binding.cardAuthor.text = card.author

        card.iconUrl?.let { url ->
            Glide.with(binding.cardImage.context).load(url)
                .transition(DrawableTransitionOptions.withCrossFade(CROSS_FADE_ANIM_DURATION_MS))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding.cardImage)
        }

        binding.root.setOnClickListener { view ->
            onItemClickListener?.onClick(card, view, position)
        }
    }

    private companion object {
        const val CROSS_FADE_ANIM_DURATION_MS = 500
    }
}
