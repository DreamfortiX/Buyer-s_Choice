package com.example.reviews.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.airbnb.lottie.LottieAnimationView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reviews.R
import com.example.reviews.data.productList
import com.example.reviews.data.Product
import com.example.reviews.data.network.CompareRequest
import com.example.reviews.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
import com.google.gson.Gson

class ComparisonActivity : AppCompatActivity() {
    private val selectedIds = linkedSetOf<String>()
    private lateinit var adapter: ProductSelectionAdapter
    private var allProducts: List<Product> = emptyList()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comparison)

        val btnCompare = findViewById<Button>(R.id.btn_compare)
        val progress = findViewById<LottieAnimationView>(R.id.progress_compare)
        val tvTopPick = findViewById<TextView>(R.id.tvTopPick)
        val topPickCard = findViewById<View>(R.id.topPickCard)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val recycler = findViewById<RecyclerView>(R.id.recyclerProducts)

        // Setup product adapter and list
        adapter = ProductSelectionAdapter(
            onSelectionChanged = { set ->
                selectedIds.clear()
                selectedIds.addAll(set)
                updateButtonState(btnCompare)
            },
            onItemClick = { _ ->
                // No-op in comparison screen; clicking name does not navigate
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // Load product list (local for now, can be from backend)
        allProducts = productList
        adapter.submitList(allProducts)

        btnCompare.setOnClickListener {
            if (selectedIds.size != 2) return@setOnClickListener
            btnCompare.isEnabled = false
            progress.visibility = View.VISIBLE
            topPickCard.visibility = View.GONE

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.api.compare(CompareRequest(productIds = selectedIds.toList()))
                    }
                    progress.visibility = View.GONE
                    btnCompare.isEnabled = true
                    // Navigate to ChartsActivity and display there
                    val json = Gson().toJson(response.products)
                    val intent = Intent(this@ComparisonActivity, ChartsActivity::class.java).apply {
                        putExtra("results_json", json)
                    }
                    startActivity(intent)
                } catch (_: Exception) {
                    progress.visibility = View.GONE
                    btnCompare.isEnabled = true
                    // Simple inline error
                    tvTopPick.text = "Failed to load comparison"
                    topPickCard.visibility = View.VISIBLE
                }
            }
        }

        updateButtonState(btnCompare)

        // Search filter
        // Keep search expanded (persistent) and not iconified
        searchView.setIconifiedByDefault(false)
        searchView.isIconified = false
        searchView.isSubmitButtonEnabled = false
        // Optionally avoid auto-focusing so keyboard doesn't open immediately
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterProducts(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText)
                return true
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateButtonState(button: Button) {
        val count = selectedIds.size
        button.isEnabled = count == 2
        button.text = "Compare Products (${count} selected)"
    }

    private fun filterProducts(query: String?) {
        val q = query?.trim()?.lowercase().orEmpty()
        val filtered = if (q.isEmpty()) allProducts else allProducts.filter { it.name.lowercase().contains(q) }
        adapter.submitList(filtered)
        // keep previous selections visually updated
        adapter.setSelected(selectedIds)
    }
}
