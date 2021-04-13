package ru.volgadev.article_galery.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.AnyThread
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ru.volgadev.article_galery.R

internal class TagsAdapter(private val itemLayout: Int) :
    RecyclerView.Adapter<TagViewHolder>() {

    interface OnItemClickListener {
        fun onClick(item: String, clickedView: CardView, position: Int)
    }

    private var onItemClickListener: OnItemClickListener? = null

    private val tags = ArrayList<String>()

    private var chosenTag: String? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TagViewHolder {
        val card =
            LayoutInflater.from(parent.context).inflate(itemLayout, parent, false) as CardView
        return TagViewHolder(card)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(position, tags[position], chosenTag, onItemClickListener)
    }

    override fun getItemCount() = tags.size

    @AnyThread
    fun setData(dataset: Collection<String>) {
        tags.clear()
        dataset.forEach { article ->
            tags.add(article)
        }
        notifyDataSetChanged()
    }

    @AnyThread
    fun getChosenTag(): String? = chosenTag

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun onChose(tag: String) {
        if (tag == chosenTag) return
        chosenTag = tag
        notifyDataSetChanged()
    }
}

internal class TagViewHolder(private val card: CardView) : RecyclerView.ViewHolder(card) {

    private val tagTextView = card.findViewById<TextView>(R.id.tagTextView)

    private var currentPosition = 0

    fun bind(
        position: Int,
        tag: String,
        chosenTag: String?,
        listener: TagsAdapter.OnItemClickListener?
    ) {
        currentPosition = position
        tagTextView.text = tag
        card.alpha = if (tag == chosenTag) 1.0f else 0.8f
        card.setOnClickListener { view ->
            listener?.onClick(tag, view as CardView, currentPosition)
        }
    }
}

