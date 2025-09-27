package com.example.reviews.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.reviews.R
import com.example.reviews.data.db.ReviewEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.VH>() {
    private val items = mutableListOf<ReviewEntity>()

    fun submit(list: List<ReviewEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSentiment: TextView = itemView.findViewById(R.id.tvSentiment)
        private val tvConfidence: TextView = itemView.findViewById(R.id.tvConfidence)
        private val tvSnippet: TextView = itemView.findViewById(R.id.tvSnippet)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun bind(item: ReviewEntity) {
            val lower = item.sentiment.lowercase(Locale.getDefault())
            tvSentiment.text = lower.uppercase(Locale.getDefault())
            val colorRes = when (lower) {
                "positive" -> R.color.positive_sentiment
                "negative" -> R.color.negative_sentiment
                else -> R.color.neutral_sentiment
            }
            tvSentiment.setTextColor(ContextCompat.getColor(itemView.context, colorRes))

            val percent = (item.confidence.coerceIn(0f, 1f) * 100).toInt()
            tvConfidence.text = "$percent%"

            // First line or snippet of the review text
            val snippet = item.text.trim().lineSequence().firstOrNull()?.take(140) ?: ""
            tvSnippet.text = snippet

            tvTimestamp.text = sdf.format(Date(item.createdAt))
        }
    }
}
