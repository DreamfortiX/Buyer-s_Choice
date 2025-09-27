package com.example.reviews.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.reviews.R
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.reviews.data_modals.FilterItem

class FilterAdapter(
    private val filters: List<FilterItem>,
    private val onItemClick: (FilterItem) -> Unit
) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardFilter: CardView = itemView.findViewById(R.id.cardFilter)
        val imageFilter: ImageView = itemView.findViewById(R.id.imageFilter)
        val textFilterTitle: TextView = itemView.findViewById(R.id.textFilterTitle)
        val textFilterSubtitle: TextView = itemView.findViewById(R.id.textFilterSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filter = filters[position]

        holder.imageFilter.setImageResource(filter.iconRes)
        holder.imageFilter.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.primary_color))
        holder.textFilterTitle.text = filter.title
        holder.textFilterSubtitle.text = filter.subtitle

        holder.cardFilter.setOnClickListener {
            onItemClick(filter)
        }

        // Entrance animation
        holder.itemView.translationY = 100f
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(position * 100L)
            .start()
    }

    override fun getItemCount(): Int = filters.size
}