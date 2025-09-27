package com.example.reviews.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reviews.R
import com.example.reviews.data.Product

class ProductSelectionAdapter(
    private val onSelectionChanged: (Set<String>) -> Unit,
    private val onItemClick: (Product) -> Unit
) : ListAdapter<Product, ProductSelectionAdapter.VH>(DIFF) {

    private val selectedIds = linkedSetOf<String>()

    fun setSelected(ids: Set<String>) {
        selectedIds.clear()
        selectedIds.addAll(ids)
        notifyDataSetChanged()
    }

    fun getSelected(): Set<String> = selectedIds

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_selection, parent, false)
        return VH(
            itemView = view,
            onToggle = { product, isChecked ->
                if (isChecked) selectedIds.add(product.id) else selectedIds.remove(product.id)
                onSelectionChanged.invoke(selectedIds)
            },
            onClick = { product -> onItemClick(product) }
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), selectedIds.contains(getItem(position).id))
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem == newItem
        }
    }

    class VH(
        itemView: View,
        private val onToggle: (Product, Boolean) -> Unit,
        private val onClick: (Product) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private lateinit var current: Product

        init {
            itemView.setOnClickListener { checkBox.performClick() }
            tvName.setOnClickListener {
                if (::current.isInitialized) onClick(current)
            }
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (::current.isInitialized) {
                    onToggle(current, isChecked)
                }
            }
        }

        fun bind(item: Product, checked: Boolean) {
            current = item
            tvName.text = item.name
            checkBox.isChecked = checked
        }
    }
}
