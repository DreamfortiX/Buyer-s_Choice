package com.example.reviews.ui.fragments


import com.example.reviews.R

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.reviews.Adapters.CategoryAdapter
import com.example.reviews.Adapters.FilterAdapter
import com.example.reviews.Adapters.ProductAdapter
import com.example.reviews.data_modals.Category
import com.example.reviews.data_modals.FilterItem
import com.example.reviews.data_modals.Product
import com.example.reviews.databinding.FragmentSearchBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.Locale

class SearchFragment : Fragment(), RecognitionListener {


    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var editSearch: EditText
    private lateinit var imageVoice: ImageView
    private lateinit var imageClear: ImageView
    private lateinit var chipGroupRecent: ChipGroup
    private lateinit var textClearRecent: TextView
    private lateinit var recyclerCategories: RecyclerView
    private lateinit var recyclerFilters: RecyclerView
    private lateinit var contentContainer: View
    private lateinit var resultsContainer: View
    private lateinit var recyclerSearchResults: RecyclerView
    private lateinit var progressBarResults: ProgressBar
    private lateinit var layoutNoResults: View
    private lateinit var textResultsCount: TextView
    private lateinit var imageFilterResults: ImageView
    private lateinit var cardVoiceSearch: CardView
    private lateinit var lottieVoice: LottieAnimationView
    private lateinit var textVoiceStatus: TextView
    private lateinit var textVoiceText: TextView
    private lateinit var textCancelVoice: TextView

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var filterAdapter: FilterAdapter
    private lateinit var searchResultsAdapter: ProductAdapter

    private val categories = mutableListOf<Category>()
    private val filters = mutableListOf<FilterItem>()
    private val searchResults = mutableListOf<Product>()
    private val recentSearches = mutableListOf<String>()

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    companion object {
        private const val RECORD_AUDIO_PERMISSION_CODE = 101
        private const val MAX_RECENT_SEARCHES = 10
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        setupAdapters()
        loadData()
        setupListeners()
        loadRecentSearches()
        setupAnimations()
        setupSpeechRecognizer()
    }

    private fun initViews() {
        editSearch = binding.editSearch
        imageVoice = binding.imageVoice
        imageClear = binding.imageClear
        chipGroupRecent = binding.chipGroupRecent
        textClearRecent = binding.textClearRecent
        recyclerCategories = binding.recyclerCategories
        recyclerFilters = binding.recyclerFilters
        contentContainer = binding.contentContainer
        resultsContainer = binding.resultsContainer
        recyclerSearchResults = binding.recyclerSearchResults
        progressBarResults = binding.progressBarResults
        layoutNoResults = binding.layoutNoResults
        textResultsCount = binding.textResultsCount
        imageFilterResults = binding.imageFilterResults
        cardVoiceSearch = binding.cardVoiceSearch
        lottieVoice = binding.lottieVoice
        textVoiceStatus = binding.textVoiceStatus
        textVoiceText = binding.textVoiceText
        textCancelVoice = binding.textCancelVoice
    }

    private fun setupAdapters() {
        // Categories adapter
        categoryAdapter = CategoryAdapter(categories) { category ->
            searchByCategory(category.name)
        }
        recyclerCategories.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerCategories.adapter = categoryAdapter

        // Filters adapter
        filterAdapter = FilterAdapter(filters) { filter ->
            applyFilter(filter)
        }
        recyclerFilters.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerFilters.adapter = filterAdapter

        // Search results adapter
        searchResultsAdapter = ProductAdapter(searchResults) { product ->
            navigateToProductDetail(product)
        }
        recyclerSearchResults.layoutManager = LinearLayoutManager(requireContext())
        recyclerSearchResults.adapter = searchResultsAdapter
    }

    private fun loadData() {
        // Load categories
        categories.addAll(listOf(
            Category("Electronics", R.drawable.ic_toys, "#4285F4"),
            Category("Fashion", R.drawable.ic_toys, "#EA4335"),
            Category("Home & Kitchen", R.drawable.ic_home, "#34A853"),
            Category("Books", R.drawable.ic_toys, "#FBBC05"),
            Category("Sports", R.drawable.ic_toys, "#9C27B0"),
            Category("Beauty", R.drawable.ic_toys, "#00BCD4"),
            Category("Toys", R.drawable.ic_toys, "#FF9800"),
            Category("Automotive", R.drawable.ic_toys, "#795548")
        ))
        categoryAdapter.notifyDataSetChanged()

        // Load filters
        filters.addAll(listOf(
            FilterItem("Price Range", "Under $100", R.drawable.ic_toys),
            FilterItem("Rating 4+", "Highly Rated", R.drawable.ic_toys),
            FilterItem("Discount", "On Sale", R.drawable.ic_toys),
            FilterItem("Popular", "Trending", R.drawable.ic_toys),
            FilterItem("Fast Delivery", "1-2 Days", R.drawable.ic_toys),
            FilterItem("Free Shipping", "No Charges", R.drawable.ic_toys)
        ))
        filterAdapter.notifyDataSetChanged()
    }

    private fun loadRecentSearches() {
        // Load from SharedPreferences
        val sharedPrefs = requireContext().getSharedPreferences("search_prefs", 0)
        val searches = sharedPrefs.getStringSet("recent_searches", emptySet()) ?: emptySet()

        recentSearches.clear()
        recentSearches.addAll(searches.take(MAX_RECENT_SEARCHES))

        updateRecentSearchesUI()
    }

    private fun saveRecentSearch(query: String) {
        if (query.isBlank()) return

        // Remove if already exists (to bring to front)
        recentSearches.remove(query)
        recentSearches.add(0, query)

        // Keep only recent searches
        if (recentSearches.size > MAX_RECENT_SEARCHES) {
            recentSearches.removeLast()
        }

        // Save to SharedPreferences
        val sharedPrefs = requireContext().getSharedPreferences("search_prefs", 0)
        val editor = sharedPrefs.edit()
        editor.putStringSet("recent_searches", recentSearches.toSet())
        editor.apply()

        updateRecentSearchesUI()
    }

    private fun updateRecentSearchesUI() {
        chipGroupRecent.removeAllViews()

        if (recentSearches.isEmpty()) {
            binding.textNoRecent.visibility = View.VISIBLE
            return
        }

        binding.textNoRecent.visibility = View.GONE

        recentSearches.forEach { query ->
            val chip = Chip(requireContext()).apply {
                text = query
                isCloseIconVisible = true
                chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.chip_background)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setOnCloseIconClickListener {
                    removeRecentSearch(query)
                }
                setOnClickListener {
                    editSearch.setText(query)
                    performSearch(query)
                }
            }
            chipGroupRecent.addView(chip)
        }
    }

    private fun removeRecentSearch(query: String) {
        recentSearches.remove(query)
        saveRecentSearchesToPrefs()
        updateRecentSearchesUI()
    }

    private fun clearAllRecentSearches() {
        recentSearches.clear()
        saveRecentSearchesToPrefs()
        updateRecentSearchesUI()
        Toast.makeText(requireContext(), "Recent searches cleared", Toast.LENGTH_SHORT).show()
    }

    private fun saveRecentSearchesToPrefs() {
        val sharedPrefs = requireContext().getSharedPreferences("search_prefs", 0)
        val editor = sharedPrefs.edit()
        editor.putStringSet("recent_searches", recentSearches.toSet())
        editor.apply()
    }

    private fun setupListeners() {
        // Search text change listener
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                imageClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE

                // Real-time search suggestions could go here
                if (query.length >= 3) {
                    // Show suggestions
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Search action (keyboard enter)
        editSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = editSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                    hideKeyboard()
                }
                true
            } else {
                false
            }
        }

        // Clear button
        imageClear.setOnClickListener {
            editSearch.setText("")
            showContentContainer()
            hideKeyboard()
        }

        // Voice search button
        imageVoice.setOnClickListener {
            startVoiceSearch()
        }

        // Clear recent searches
        textClearRecent.setOnClickListener {
            clearAllRecentSearches()
        }

        // Filter results button
        imageFilterResults.setOnClickListener {
            showAdvancedFilters()
        }

        // Cancel voice search
        textCancelVoice.setOnClickListener {
            stopVoiceSearch()
        }
    }

    private fun setupAnimations() {
        // Entrance animations for categories and filters
        val categories = listOf(binding.textCategoriesTitle, binding.recyclerCategories)
        val filters = listOf(binding.textFiltersTitle, binding.recyclerFilters)

        categories.forEachIndexed { index, view ->
            view.translationY = 30f
            view.alpha = 0f

            view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(index * 100L)
                .start()
        }

        filters.forEachIndexed { index, view ->
            view.translationY = 30f
            view.alpha = 0f

            view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(200 + index * 100L)
                .start()
        }
    }

    private fun setupSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            speechRecognizer?.setRecognitionListener(this)
        }
    }

    private fun checkAudioPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
            false
        }
    }

    private fun startVoiceSearch() {
        if (!checkAudioPermission()) return

        // Show voice search dialog
        cardVoiceSearch.visibility = View.VISIBLE
        cardVoiceSearch.alpha = 0f
        cardVoiceSearch.scaleX = 0.8f
        cardVoiceSearch.scaleY = 0.8f

        cardVoiceSearch.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .start()

        lottieVoice.playAnimation()
        textVoiceStatus.text = "Listening..."
        textVoiceText.text = "Speak now"

        // Start speech recognition
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.startListening(intent)
        isListening = true
    }

    private fun stopVoiceSearch() {
        speechRecognizer?.stopListening()
        isListening = false

        cardVoiceSearch.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(300)
            .withEndAction {
                cardVoiceSearch.visibility = View.GONE
            }
            .start()
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) return

        saveRecentSearch(query)
        showResultsContainer()

        // Show loading
        progressBarResults.visibility = View.VISIBLE
        recyclerSearchResults.visibility = View.GONE
        layoutNoResults.visibility = View.GONE

        // Simulate API call delay
        Handler(Looper.getMainLooper()).postDelayed({
            // Clear previous results
            searchResults.clear()

            // Simulate search results
            when (query.lowercase()) {
                "headphones", "earphones" -> {
                    searchResults.addAll(getHeadphoneProducts())
                }
                "laptop", "notebook" -> {
                    searchResults.addAll(getLaptopProducts())
                }
                "smartwatch", "watch" -> {
                    searchResults.addAll(getSmartwatchProducts())
                }
                "phone", "smartphone" -> {
                    searchResults.addAll(getPhoneProducts())
                }
                else -> {
                    // Generic search results
                    searchResults.addAll(getGenericProducts())
                }
            }

            updateSearchResultsUI()
        }, 1000)
    }

    private fun searchByCategory(category: String) {
        editSearch.setText(category)
        performSearch(category)
    }

    private fun applyFilter(filter: FilterItem) {
        Toast.makeText(requireContext(), "Filter applied: ${filter.title}", Toast.LENGTH_SHORT).show()

        // If we're in search results, re-filter them
        if (resultsContainer.visibility == View.VISIBLE) {
            // Apply filter logic here
            val query = editSearch.text.toString()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }
    }

    private fun showAdvancedFilters() {
        // Navigate to advanced filters activity/fragment
        Toast.makeText(requireContext(), "Advanced Filters", Toast.LENGTH_SHORT).show()
    }

    private fun showContentContainer() {
        contentContainer.visibility = View.VISIBLE
        resultsContainer.visibility = View.GONE

        contentContainer.alpha = 0f
        contentContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun showResultsContainer() {
        contentContainer.visibility = View.GONE
        resultsContainer.visibility = View.VISIBLE

        resultsContainer.alpha = 0f
        resultsContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun updateSearchResultsUI() {
        progressBarResults.visibility = View.GONE

        if (searchResults.isEmpty()) {
            recyclerSearchResults.visibility = View.GONE
            layoutNoResults.visibility = View.VISIBLE
            textResultsCount.text = "No Results"
        } else {
            recyclerSearchResults.visibility = View.VISIBLE
            layoutNoResults.visibility = View.GONE
            textResultsCount.text = "${searchResults.size} Results Found"
            searchResultsAdapter.notifyDataSetChanged()

            // Add entrance animation for results
            recyclerSearchResults.itemAnimator = null
            for (i in 0 until recyclerSearchResults.childCount) {
                val view = recyclerSearchResults.getChildAt(i)
                view.translationY = 50f
                view.alpha = 0f

                view.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(i * 100L)
                    .start()
            }
        }
    }

    private fun navigateToProductDetail(product: Product) {
        // Navigate to product detail
        Toast.makeText(requireContext(), "Opening ${product.title}", Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(editSearch.windowToken, 0)
    }

    // Speech Recognition Listeners
    override fun onReadyForSpeech(params: Bundle?) {
        textVoiceStatus.text = "Listening..."
        textVoiceText.text = "Speak now"
    }

    override fun onBeginningOfSpeech() {
        textVoiceStatus.text = "Listening..."
        textVoiceText.text = ""
    }

    override fun onRmsChanged(rmsdB: Float) {
        // You can update animation based on volume
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        // Not used
    }

    override fun onEndOfSpeech() {
        textVoiceStatus.text = "Processing..."
        textVoiceText.text = "Recognizing speech..."
    }

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }

        Toast.makeText(requireContext(), "Voice search error: $errorMessage", Toast.LENGTH_SHORT).show()
        stopVoiceSearch()
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val spokenText = matches[0]
            textVoiceText.text = spokenText

            // Update search field and perform search
            editSearch.setText(spokenText)
            Handler(Looper.getMainLooper()).postDelayed({
                performSearch(spokenText)
                stopVoiceSearch()
            }, 1000)
        } else {
            Toast.makeText(requireContext(), "No speech recognized", Toast.LENGTH_SHORT).show()
            stopVoiceSearch()
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            textVoiceText.text = matches[0]
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // Not used
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceSearch()
            } else {
                Toast.makeText(requireContext(), "Permission denied for voice search", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speechRecognizer?.destroy()
        _binding = null
    }

    // Dummy data functions
    private fun getHeadphoneProducts(): List<Product> {
        return listOf(
            Product(
                id = "1",
                title = "Sony WH-1000XM4 Noise Cancelling Headphones",
                category = "Electronics • Audio",
                price = 299.99,
                originalPrice = 349.99,
                rating = 4.8,
                reviews = 8200,
                imageUrl = "",
                popularityScore = 9.2,
                discount = 14
            ),
            Product(
                id = "2",
                title = "Bose QuietComfort 45 Wireless",
                category = "Electronics • Audio",
                price = 329.99,
                originalPrice = 379.99,
                rating = 4.7,
                reviews = 6100,
                imageUrl = "",
                popularityScore = 8.5,
                discount = 13
            ),
            Product(
                id = "3",
                title = "Apple AirPods Pro (2nd Generation)",
                category = "Electronics • Audio",
                price = 249.99,
                originalPrice = 279.99,
                rating = 4.6,
                reviews = 12500,
                imageUrl = "",
                popularityScore = 9.0,
                discount = 11
            )
        )
    }

    private fun getLaptopProducts(): List<Product> {
        return listOf(
            Product(
                id = "4",
                title = "MacBook Pro 16-inch M2 Max",
                category = "Electronics • Laptops",
                price = 2399.99,
                originalPrice = 2699.99,
                rating = 4.9,
                reviews = 5400,
                imageUrl = "",
                popularityScore = 9.5,
                discount = 11
            ),
            Product(
                id = "5",
                title = "Dell XPS 15 OLED Touch Laptop",
                category = "Electronics • Laptops",
                price = 1899.99,
                originalPrice = 2199.99,
                rating = 4.7,
                reviews = 3200,
                imageUrl = "",
                popularityScore = 8.8,
                discount = 14
            )
        )
    }

    private fun getSmartwatchProducts(): List<Product> {
        return listOf(
            Product(
                id = "6",
                title = "Apple Watch Series 8 GPS",
                category = "Electronics • Wearables",
                price = 399.99,
                originalPrice = 429.99,
                rating = 4.8,
                reviews = 8900,
                imageUrl = "",
                popularityScore = 9.1,
                discount = 7
            ),
            Product(
                id = "7",
                title = "Samsung Galaxy Watch 5 Pro",
                category = "Electronics • Wearables",
                price = 449.99,
                originalPrice = 499.99,
                rating = 4.6,
                reviews = 4200,
                imageUrl = "",
                popularityScore = 8.3,
                discount = 10
            )
        )
    }

    private fun getPhoneProducts(): List<Product> {
        return listOf(
            Product(
                id = "8",
                title = "iPhone 14 Pro Max",
                category = "Electronics • Smartphones",
                price = 1099.99,
                rating = 4.8,
                reviews = 15200,
                imageUrl = "",
                popularityScore = 9.4,
                discount = null
            ),
            Product(
                id = "9",
                title = "Samsung Galaxy S23 Ultra",
                category = "Electronics • Smartphones",
                price = 1199.99,
                rating = 4.7,
                reviews = 9800,
                imageUrl = "",
                popularityScore = 9.1,
                discount = null
            )
        )
    }

    private fun getGenericProducts(): List<Product> {
        return listOf(
            Product(
                id = "10",
                title = "Search Result Product",
                category = "Various • Categories",
                price = 99.99,
                rating = 4.5,
                reviews = 1000,
                imageUrl = "",
                popularityScore = 7.5,
                discount = 20
            )
        )
    }
}
