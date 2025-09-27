package com.example.reviews.Adapters

import com.example.reviews.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.reviews.data_modals.OnboardingItem

class OnboardingAdapter(
    private val items: List<OnboardingItem>,
    private val onCardClick: (Int) -> Unit
) : RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lottieAnimation: LottieAnimationView = itemView.findViewById(R.id.lottieAnimation)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textDescription: TextView = itemView.findViewById(R.id.textDescription)
        val cardCosine: CardView = itemView.findViewById(R.id.cardCosine)
        val cardDecisionTree: CardView = itemView.findViewById(R.id.cardDecisionTree)
        val cardGNN: CardView = itemView.findViewById(R.id.cardGNN)
        val iconCosine: ImageView = itemView.findViewById(R.id.iconCosine)
        val iconDecisionTree: ImageView = itemView.findViewById(R.id.iconDecisionTree)
        val iconGNN: ImageView = itemView.findViewById(R.id.iconGNN)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Set basic data
        holder.lottieAnimation.setAnimation(item.lottieAnimation)
        holder.textTitle.text = item.title
        holder.textDescription.text = item.description

        // Reset all cards
        resetCard(holder.cardCosine, holder.iconCosine, R.color.cosine_color)
        resetCard(holder.cardDecisionTree, holder.iconDecisionTree, R.color.decision_tree_color)
        resetCard(holder.cardGNN, holder.iconGNN, R.color.gnn_color)

        // Highlight active card
        when (item.activeCard) {
            0 -> highlightCard(holder.cardCosine, holder.iconCosine, R.color.cosine_color)
            1 -> highlightCard(holder.cardDecisionTree, holder.iconDecisionTree, R.color.decision_tree_color)
            2 -> highlightCard(holder.cardGNN, holder.iconGNN, R.color.gnn_color)
        }

        // Set click listeners
        holder.cardCosine.setOnClickListener { onCardClick(0) }
        holder.cardDecisionTree.setOnClickListener { onCardClick(1) }
        holder.cardGNN.setOnClickListener { onCardClick(2) }

        // Start animation
        holder.lottieAnimation.playAnimation()
    }

    private fun highlightCard(card: CardView, icon: ImageView, colorRes: Int) {
        card.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .alpha(1f)
            .setDuration(300)
            .start()

        card.cardElevation = 16f

        icon.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(300)
            .start()
    }

    private fun resetCard(card: CardView, icon: ImageView, colorRes: Int) {
        card.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(0.5f)
            .setDuration(300)
            .start()

        card.cardElevation = 8f

        icon.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .start()
    }

    override fun getItemCount(): Int = items.size
}