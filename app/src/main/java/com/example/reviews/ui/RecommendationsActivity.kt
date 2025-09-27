package com.example.reviews.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reviews.R
import com.example.reviews.data.Product
import com.example.reviews.data.productList
import com.google.android.material.appbar.MaterialToolbar

class RecommendationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendations)

        // Toolbar setup with Settings menu and back navigation
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        toolbar.inflateMenu(R.menu.menu_main)

        val tvFeatured = findViewById<TextView>(R.id.tvFeaturedTitle)
        val recycler = findViewById<RecyclerView>(R.id.recyclerAlternatives)

        // For now, use top-rated by simple heuristic: longer name last char as pseudo rating
        // Replace with backend /recommendations when ready
        val items: List<Product> = productList
        val featured = items.firstOrNull()
        tvFeatured.text = featured?.name ?: getString(R.string.app_name)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = object : RecyclerView.Adapter<VH>() {
            private val data = if (items.size > 1) items.drop(1) else emptyList()
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
                val v = android.view.LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)
                return VH(v as TextView)
            }
            override fun getItemCount(): Int = data.size
            override fun onBindViewHolder(holder: VH, position: Int) {
                holder.tv.text = "• ${data[position].name}"
            }
        }
    }

    class VH(val tv: TextView) : RecyclerView.ViewHolder(tv)
}
