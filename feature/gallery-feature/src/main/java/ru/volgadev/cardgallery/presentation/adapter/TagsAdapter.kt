package ru.volgadev.cardgallery.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ru.volgadev.cardgallery.databinding.CategoryTagBinding

internal class TagsAdapter : RecyclerView.Adapter<TagViewHolder>() {

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
        val binding = CategoryTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]
        holder.bind(position, tag, isChosen = tag == chosenTag, onItemClickListener)
    }

    override fun getItemCount() = tags.size

    fun setData(dataset: Collection<String>) {
        tags.clear()
        dataset.forEach { tags.add(it) }
        notifyDataSetChanged()
    }

    fun getChosenTag(): String? = chosenTag

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun onChoose(tag: String) {
        if (tag == chosenTag) return
        chosenTag = tag
        notifyDataSetChanged()
    }
}

internal class TagViewHolder(private val binding: CategoryTagBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        position: Int,
        tag: String,
        isChosen: Boolean,
        listener: TagsAdapter.OnItemClickListener?
    ) {
        binding.tagTextView.text = tag
        binding.root.alpha = if (isChosen) CHOSEN_ALPHA else NO_CHOSEN_ALPHA
        binding.root.setOnClickListener { view ->
            listener?.onClick(tag, view as CardView, position)
        }
    }

    private companion object {
        const val CHOSEN_ALPHA = 1.0f
        const val NO_CHOSEN_ALPHA = 0.8f
    }
}

