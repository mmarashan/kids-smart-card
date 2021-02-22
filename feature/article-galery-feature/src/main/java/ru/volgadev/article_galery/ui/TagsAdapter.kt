package ru.volgadev.article_galery.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ru.volgadev.article_galery.R
import ru.volgadev.common.log.Logger

class TagsAdapter(context: Context, private val itemLayout: Int) :
    RecyclerView.Adapter<TagsAdapter.ViewHolder>() {

    private val logger = Logger.get("TagsAdapter")

    interface OnItemClickListener {
        fun onClick(item: String, clickedView: CardView, position: Int)
    }

    private var onItemClickListener: OnItemClickListener? = null

    private val layoutInflater by lazy { LayoutInflater.from(context) }

    private val tags = ArrayList<String>()

    private var chosenTag: String? = null

    @AnyThread
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    @MainThread
    fun onChose(tag: String) {
        logger.debug("showChosen($tag)")
        if (tag == chosenTag) return
        chosenTag = tag
        notifyDataSetChanged()
    }

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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val card = layoutInflater.inflate(itemLayout, parent, false) as CardView
        return ViewHolder(card)
    }

    inner class ViewHolder(private val card: CardView) : RecyclerView.ViewHolder(card),
        View.OnClickListener {

        private val tagTextView = card.findViewById<TextView>(R.id.tagTextView)

        private var currentPosition = 0

        fun bind(position: Int, tag: String) {
            val holder = this
            holder.currentPosition = position
            card.tag = tag
            holder.tagTextView.text = tag
            if (tag == chosenTag) {
                card.alpha = 1.0f
            } else {
                card.alpha = 0.8f
            }
            holder.card.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            view?.let { view ->
                val id = view.tag as String
                logger.debug("On click $id")
                onItemClickListener?.onClick(id, view as CardView, currentPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, tags[position])
    }

    override fun getItemCount() = tags.size
}
