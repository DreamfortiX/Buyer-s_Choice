package com.example.reviews.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reviews.Adapters.ProductAdapter
import com.example.reviews.Adapters.TrendingProductAdapter
import com.example.reviews.R
import com.example.reviews.data_modals.Product
import com.example.reviews.ui.FilterActivity
import com.example.reviews.ui.PersonalizedActivity
import com.example.reviews.ui.ProductDetailActivity
import com.example.reviews.ui.RecommendationActivity
import com.example.reviews.ui.TrendingActivity

class HomeFragment : Fragment() {

    private lateinit var editSearch: EditText
    private lateinit var cardCosineMethod: CardView
    private lateinit var cardDTMethod: CardView
    private lateinit var cardGNNMethod: CardView
    private lateinit var recyclerTrending: RecyclerView
    private lateinit var recyclerPersonalized: RecyclerView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var loadingAnimation: ProgressBar
    private lateinit var textLoading: TextView
    private lateinit var imageFilter: ImageView

    private lateinit var trendingAdapter: TrendingProductAdapter
    private lateinit var personalizedAdapter: ProductAdapter

    private val trendingProducts = mutableListOf<Product>()
    private val personalizedProducts = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerViews()
        setupClickListeners()
        loadDummyData()
        setupAnimations(view)
        setupSearch()
    }

    private fun initViews(view: View) {
        editSearch = view.findViewById(R.id.editSearch)
        cardCosineMethod = view.findViewById(R.id.cardCosineMethod)
        cardDTMethod = view.findViewById(R.id.cardDTMethod)
        cardGNNMethod = view.findViewById(R.id.cardGNNMethod)
        recyclerTrending = view.findViewById(R.id.recyclerTrending)
        recyclerPersonalized = view.findViewById(R.id.recyclerPersonalized)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
        loadingAnimation = view.findViewById(R.id.loadingProgress)
        textLoading = view.findViewById(R.id.textLoading)
        imageFilter = view.findViewById(R.id.imageFilter)

        // View All buttons
        view.findViewById<TextView>(R.id.textViewAllTrending).setOnClickListener {
            startActivity(Intent(requireContext(), TrendingActivity::class.java))
        }

        view.findViewById<TextView>(R.id.textViewAllPersonalized).setOnClickListener {
            startActivity(Intent(requireContext(), PersonalizedActivity::class.java))
        }
    }

    private fun setupRecyclerViews() {
        // Trending Products (Horizontal)
        trendingAdapter = TrendingProductAdapter(trendingProducts) { product ->
            showProductDetail(product)
        }
        recyclerTrending.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerTrending.adapter = trendingAdapter

        // Personalized Products (Vertical)
        personalizedAdapter = ProductAdapter(personalizedProducts) { product ->
            showProductDetail(product)
        }
        recyclerPersonalized.layoutManager = LinearLayoutManager(requireContext())
        recyclerPersonalized.adapter = personalizedAdapter
    }

    private fun setupClickListeners() {
        // Method Cards Click Listeners
        cardCosineMethod.setOnClickListener {
            showMethodLoading("Cosine Similarity")
            simulateRecommendation("cosine")
        }

        cardDTMethod.setOnClickListener {
            showMethodLoading("Decision Tree")
            simulateRecommendation("decision_tree")
        }

        cardGNNMethod.setOnClickListener {
            showMethodLoading("GNN")
            simulateRecommendation("gnn")
        }

        // Filter icon
        imageFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showMethodLoading(methodName: String) {
        textLoading.text = "Analyzing with $methodName..."
        loadingOverlay.visibility = View.VISIBLE
        loadingAnimation.visibility = View.VISIBLE

        // Add entrance animation
        loadingOverlay.alpha = 0f
        loadingOverlay.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun hideMethodLoading() {
        loadingOverlay.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                loadingOverlay.visibility = View.GONE
                loadingAnimation.visibility = View.GONE
            }
            .start()
    }

    private fun simulateRecommendation(method: String) {
        // Simulate API call delay
        Handler(Looper.getMainLooper()).postDelayed({
            // Update loading text
            textLoading.text = "Generating recommendations..."
            loadingAnimation.visibility = View.VISIBLE

            // Another delay for processing
            Handler(Looper.getMainLooper()).postDelayed({
                hideMethodLoading()

                // Show success toast
                val methodName = when (method) {
                    "cosine" -> "Cosine Similarity"
                    "decision_tree" -> "Decision Tree"
                    else -> "GNN"
                }
                Toast.makeText(requireContext(), "Recommendations generated using $methodName!", Toast.LENGTH_SHORT).show()

                // Navigate to recommendation results
                val intent = Intent(requireContext(), RecommendationActivity::class.java).apply {
                    putExtra("method", method)
                }
                startActivity(intent)

                // Add slide animation
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }, 1500)
        }, 2000)
    }

    private fun loadDummyData() {
        // Clear existing data
        trendingProducts.clear()
        personalizedProducts.clear()

        // Add trending products
        trendingProducts.addAll(listOf(
            Product(
                id = "1",
                title = "Apple iPhone 13 Pro Max",
                category = "Electronics • Smartphones",
                price = 999.99,
                originalPrice = 1099.99,
                rating = 4.7,
                reviews = 12500,
                imageUrl = "https://example.com/iphone.jpg",
                popularityScore = 8.7,
                discount = 9
            ),
            Product(
                id = "2",
                title = "Sony WH-1000XM4 Headphones",
                category = "Electronics • Audio",
                price = 299.99,
                originalPrice = 349.99,
                rating = 4.8,
                reviews = 8200,
                imageUrl = "https://example.com/sony.jpg",
                popularityScore = 9.2,
                discount = 14
            ),
            Product(
                id = "3",
                title = "MacBook Pro 16-inch",
                category = "Electronics • Laptops",
                price = 2399.99,
                originalPrice = 2699.99,
                rating = 4.9,
                reviews = 5400,
                imageUrl = "https://example.com/macbook.jpg",
                popularityScore = 9.5,
                discount = 11
            ),
            Product(
                id = "4",
                title = "Samsung Galaxy Watch 5",
                category = "Electronics • Wearables",
                price = 279.99,
                originalPrice = 329.99,
                rating = 4.6,
                reviews = 3200,
                imageUrl = "https://example.com/watch.jpg",
                popularityScore = 8.3,
                discount = 15
            )
        ))

        // Add personalized products
        personalizedProducts.addAll(listOf(
            Product(
                id = "5",
                title = "Google Pixel 7 Pro",
                category = "Electronics • Smartphones",
                price = 899.99,
                originalPrice = 999.99,
                rating = 4.6,
                reviews = 8900,
                imageUrl = "https://example.com/pixel.jpg",
                popularityScore = 8.9,
                discount = 10
            ),
            Product(
                id = "6",
                title = "Bose QuietComfort 45",
                category = "Electronics • Audio",
                price = 329.99,
                originalPrice = 379.99,
                rating = 4.7,
                reviews = 6100,
                imageUrl = "https://example.com/bose.jpg",
                popularityScore = 8.5,
                discount = 13
            ),
            Product(
                id = "7",
                title = "iPad Pro 12.9-inch",
                category = "Electronics • Tablets",
                price = 1099.99,
                originalPrice = 1199.99,
                rating = 4.8,
                reviews = 7200,
                imageUrl = "https://example.com/ipad.jpg",
                popularityScore = 9.1,
                discount = 8
            ),
            Product(
                id = "8",
                title = "Dyson V11 Vacuum",
                category = "Home • Appliances",
                price = 599.99,
                originalPrice = 699.99,
                rating = 4.5,
                reviews = 4300,
                imageUrl = "https://example.com/dyson.jpg",
                popularityScore = 8.0,
                discount = 14
            )
        ))

        trendingAdapter.notifyDataSetChanged()
        personalizedAdapter.notifyDataSetChanged()
    }

    private fun setupAnimations(view: View) {
        // Entrance animation for method cards
        val cards = listOf(cardCosineMethod, cardDTMethod, cardGNNMethod)

        cards.forEachIndexed { index, card ->
            card.translationY = 50f
            card.alpha = 0f

            card.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(index * 100L)
                .start()
        }

        // Pulse animation for search bar
        val searchCard = view.findViewById<CardView>(R.id.cardSearch)
        searchCard.translationY = -30f
        searchCard.alpha = 0f

        searchCard.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(600)
            .setInterpolator(BounceInterpolator())
            .start()
    }

    private fun setupSearch() {
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called when the text is being changed
            }

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.isNotEmpty()) {
                        // Show search results
                        performSearch(it.toString())
                    } else {
                        // Clear search results or show default state
                    }
                }
            }
        })
    }

    private fun performSearch(query: String) {
        // In real app, this would call API
        // For now, just show a toast
        if (query.length > 2) {
            Toast.makeText(requireContext(), "Searching for: $query", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFilterDialog() {
        // Show filter dialog
        val intent = Intent(requireContext(), FilterActivity::class.java)
        startActivityForResult(intent, 100)
    }

    private fun showProductDetail(product: Product) {
        val intent = Intent(requireContext(), ProductDetailActivity::class.java).apply {
            putExtra("product_id", product.id)
            putExtra("product_title", product.title)
            putExtra("product_price", product.price)
            putExtra("product_rating", product.rating)
            putExtra("product_popularity", product.popularityScore)
        }
        startActivity(intent)

        // Add slide animation
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Filters applied
            Toast.makeText(requireContext(), "Filters applied!", Toast.LENGTH_SHORT).show()
        }
    }
}