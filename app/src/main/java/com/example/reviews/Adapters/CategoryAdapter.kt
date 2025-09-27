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
import com.example.reviews.data_modals.Category
import java.util.Locale

class CategoryAdapter(
    private val categories: List<Category>,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardCategory: CardView = itemView.findViewById(R.id.cardCategory)
        val imageCategory: ImageView = itemView.findViewById(R.id.imageCategory)
        val textCategoryName: TextView = itemView.findViewById(R.id.textCategoryName)
        val textProductCount: TextView = itemView.findViewById(R.id.textProductCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]

        holder.imageCategory.setImageResource(category.iconRes)
        holder.imageCategory.setColorFilter(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
        holder.textCategoryName.text = category.name
        holder.textProductCount.text = "1.2K products"

        // Set background color
        holder.cardCategory.setCardBackgroundColor(android.graphics.Color.parseColor(category.color))

        // Add click animation
        holder.cardCategory.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                    onItemClick(category)
                }
                .start()
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

    override fun getItemCount(): Int = categories.size
}