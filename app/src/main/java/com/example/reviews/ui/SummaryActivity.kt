package com.example.reviews.ui

import android.os.Bundle
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import com.airbnb.lottie.LottieAnimationView
import androidx.appcompat.app.AppCompatActivity
import com.example.reviews.R
import com.bumptech.glide.Glide
import androidx.lifecycle.lifecycleScope
import com.example.reviews.data.network.ApiService
import com.example.reviews.data.network.RetrofitClient
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reviews.data.productList
import com.example.reviews.data.Product
import androidx.appcompat.widget.SearchView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.facebook.shimmer.ShimmerFrameLayout
import com.example.reviews.data.db.AppDatabase
import com.example.reviews.data.db.SummaryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SummaryActivity : AppCompatActivity() {
    private lateinit var progress: LottieAnimationView
    private lateinit var image: ImageView
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ProductSelectionAdapter
    private lateinit var searchView: SearchView
    private lateinit var btnSummary: MaterialButton
    private lateinit var shimmer: ShimmerFrameLayout
    private var allProducts: List<Product> = emptyList()
    private var selectedIds: LinkedHashSet<String> = linkedSetOf()
    private var latestSummaryText: String? = null
    private var latestProductName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        searchView = findViewById(R.id.searchView)
        recycler = findViewById(R.id.recyclerProductsSummary)
        progress = findViewById(R.id.progress_compare)
        btnSummary = findViewById(R.id.btn_compare)
        image = findViewById(R.id.image_word_cloud)
        shimmer = findViewById(R.id.shimmer_container)

        // Setup products list using the same adapter as Comparison
        adapter = ProductSelectionAdapter(
            onSelectionChanged = { set ->
                selectedIds.clear(); selectedIds.addAll(set)
                btnSummary.isEnabled = selectedIds.size == 1
            },
            onItemClick = { product ->
                // Do not start summarizing on item click. Just select this product.
                selectedIds.clear()
                selectedIds.add(product.id)
                adapter.setSelected(selectedIds)
                btnSummary.isEnabled = true
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        allProducts = productList
        adapter.submitList(allProducts)

        // Search persistent
        searchView.setIconifiedByDefault(false)
        searchView.isIconified = false
        searchView.isSubmitButtonEnabled = false
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { filterProducts(query); return true }
            override fun onQueryTextChange(newText: String?): Boolean { filterProducts(newText); return true }
        })

        // Button triggers summary for selected single product
        btnSummary.setOnClickListener {
            if (selectedIds.size == 1) {
                val id = selectedIds.first()
                val prod = allProducts.firstOrNull { it.id == id } ?: Product(id, id)
                fetchAndShowSummary(prod)
            }
        }

        // If activity started with a product, pre-select it but do NOT auto-summarize
        val productId = intent.getStringExtra("product_id")
        val productName = intent.getStringExtra("product_name")
        if (!productId.isNullOrBlank()) {
            selectedIds.clear()
            selectedIds.add(productId)
            adapter.setSelected(selectedIds)
            btnSummary.isEnabled = true
        }
    }

    private fun filterProducts(query: String?) {
        val q = query?.trim()?.lowercase().orEmpty()
        val filtered = if (q.isEmpty()) allProducts else allProducts.filter { it.name.lowercase().contains(q) }
        adapter.submitList(filtered)
        adapter.setSelected(selectedIds)
    }

    private fun fetchAndShowSummary(product: Product) {
        progress.visibility = View.VISIBLE
        image.setImageDrawable(null)
        latestSummaryText = null
        latestProductName = product.name

        // Try cached first
        val dao = AppDatabase.get(applicationContext).summaryDao()
        lifecycleScope.launch {
            val cached = withContext(Dispatchers.IO) { dao.get(product.id) }
            if (cached != null) {
                latestSummaryText = cached.summaryText
                latestProductName = cached.productName
                findViewById<android.widget.TextView>(R.id.tv_summary_inline).text = cached.summaryText
                invalidateOptionsMenu()
                if (cached.wordCloudUrl.isNotBlank()) {
                    image.visibility = View.VISIBLE
                    Glide.with(this@SummaryActivity)
                        .load(cached.wordCloudUrl)
                        .placeholder(R.drawable.rounded_white_alpha)
                        .into(image)
                } else {
                    image.visibility = View.GONE
                }
            }

            // Proceed to network to refresh while showing cached
            shimmer.startShimmer()
            shimmer.visibility = View.VISIBLE

            try {
                val response: ApiService.SummarizeResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.api.summarize(ApiService.SummarizeRequest(product.id))
                }
                progress.visibility = View.GONE
                // Build display text: paragraph summary + key insights as bullets
                val insightsBullets = if (response.keyInsights.isNotEmpty()) {
                    "\n\n" + response.keyInsights.joinToString(separator = "\n") { "• $it" }
                } else ""
                val combinedText = response.summary + insightsBullets
                latestSummaryText = combinedText
                // Update inline summary
                findViewById<android.widget.TextView>(R.id.tv_summary_inline).text = combinedText
                invalidateOptionsMenu()
                // Show a quick confirmation toast; summary ready
                Toast.makeText(this@SummaryActivity, "Summary ready for ${product.name}", Toast.LENGTH_SHORT).show()
                // Backend returns word frequencies; if you later add a URL, load it here. For now, hide the image.
                image.visibility = View.GONE
                shimmer.stopShimmer()
                shimmer.visibility = View.GONE

                // Save to cache
                withContext(Dispatchers.IO) {
                    dao.upsert(
                        SummaryEntity(
                            productId = product.id,
                            productName = product.name,
                            summaryText = combinedText,
                            // No URL provided in new API response; store empty to indicate none
                            wordCloudUrl = "",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            } catch (e: Exception) {
                progress.visibility = View.GONE
                Toast.makeText(this@SummaryActivity, "Failed to load summary. Please try again.", Toast.LENGTH_SHORT).show()
                // Offer retry via Snackbar
                Snackbar.make(recycler, R.string.loading_app_data, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) { fetchAndShowSummary(product) }
                    .show()
                shimmer.stopShimmer()
                shimmer.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_summary, menu)
        // Disable share until we have content
        val shareItem = menu.findItem(R.id.action_share)
        shareItem.isEnabled = latestSummaryText?.isNotBlank() == true
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val shareItem = menu.findItem(R.id.action_share)
        shareItem?.isEnabled = latestSummaryText?.isNotBlank() == true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                val text = latestSummaryText
                if (!text.isNullOrBlank()) {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_SUBJECT, "Review Summary: ${latestProductName ?: "Product"}")
                        putExtra(Intent.EXTRA_TEXT, text)
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.app_name)))
                } else {
                    Toast.makeText(this, "No summary yet. Select a product first.", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

