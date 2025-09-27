package com.example.reviews.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.reviews.R
import com.example.reviews.data_modals.Product

class ProductAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageProduct: ImageView = itemView.findViewById(R.id.imageProduct)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textCategory: TextView = itemView.findViewById(R.id.textCategory)
        val textRating: TextView = itemView.findViewById(R.id.textRating)
        val textReviews: TextView = itemView.findViewById(R.id.textReviews)
        val textPrice: TextView = itemView.findViewById(R.id.textPrice)
        val textOriginalPrice: TextView = itemView.findViewById(R.id.textOriginalPrice)
        val textPopularity: TextView = itemView.findViewById(R.id.textPopularity)
        val imageFavorite: ImageView = itemView.findViewById(R.id.imageFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_vertical, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]

        // Load image
        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .placeholder(R.drawable.placeholder_product)
            .into(holder.imageProduct)

        holder.textTitle.text = product.title
        holder.textCategory.text = product.category
        holder.textRating.text = product.rating.toString()
        holder.textReviews.text = "(${product.reviews} reviews)"
        holder.textPrice.text = "$${product.price}"
        holder.textPopularity.text = "🔥 ${product.popularityScore}"

        // Show original price if available
        product.originalPrice?.let {
            holder.textOriginalPrice.text = "$$it"
            holder.textOriginalPrice.visibility = View.VISIBLE
        }

        // Set favorite icon
        val favoriteRes = if (product.isFavorite) {
            R.drawable.ic_favorite_filled
        } else {
            R.drawable.ic_favorite_border
        }
        holder.imageFavorite.setImageResource(favoriteRes)
        holder.imageFavorite.setOnClickListener {
            product.isFavorite = !product.isFavorite
            notifyItemChanged(position)
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = products.size
}