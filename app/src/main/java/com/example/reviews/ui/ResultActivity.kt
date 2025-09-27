package com.example.reviews.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.reviews.R
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import com.example.reviews.data.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val sentiment = intent.getStringExtra("sentiment") ?: "unknown"
        val confidence = intent.getFloatExtra("confidence", 0f)
        val reviewText = intent.getStringExtra("reviewText")
        val distPos = intent.getIntExtra("dist_positive", -1)
        val distNeu = intent.getIntExtra("dist_neutral", -1)
        val distNeg = intent.getIntExtra("dist_negative", -1)

        val sentimentText = findViewById<TextView>(R.id.sentimentText)
        val confidencePercent = findViewById<TextView>(R.id.confidencePercent)
        val progressBar = findViewById<ProgressBar>(R.id.confidenceProgress)
        val analyzeAnotherButton = findViewById<Button>(R.id.analyzeAnotherButton)
        val saveResultButton = findViewById<Button>(R.id.saveResultButton)
        val compareButton = findViewById<Button>(R.id.compareButton)

        val distributionSection = findViewById<View>(R.id.distributionSection)
        val barPositive = findViewById<ProgressBar>(R.id.barPositive)
        val barNeutral = findViewById<ProgressBar>(R.id.barNeutral)
        val barNegative = findViewById<ProgressBar>(R.id.barNegative)
        val percentPositive = findViewById<TextView>(R.id.percentPositive)
        val percentNeutral = findViewById<TextView>(R.id.percentNeutral)
        val percentNegative = findViewById<TextView>(R.id.percentNegative)

        // Set sentiment text and color
        val lower = sentiment.lowercase()
        sentimentText.text = sentiment.uppercase()
        val colorRes = when (lower) {
            "positive" -> R.color.positive_sentiment
            "negative" -> R.color.negative_sentiment
            else -> R.color.neutral_sentiment
        }
        sentimentText.setTextColor(ContextCompat.getColor(this, colorRes))

        // Confidence UI
        val percent = (confidence.coerceIn(0f, 1f) * 100).toInt()
        progressBar.progress = percent
        confidencePercent.text = "$percent%"

        // Sentiment distribution (optional)
        if (distPos >= 0 && distNeu >= 0 && distNeg >= 0) {
            distributionSection.visibility = View.VISIBLE
            barPositive.progress = distPos
            barNeutral.progress = distNeu
            barNegative.progress = distNeg
            percentPositive.text = "$distPos%"
            percentNeutral.text = "$distNeu%"
            percentNegative.text = "$distNeg%"
        } else {
            distributionSection.visibility = View.GONE
        }

        // Actions
        analyzeAnotherButton.setOnClickListener {
            // simply finish to go back to previous (e.g., analyze/home)
            onBackPressedDispatcher.onBackPressed()
        }

        compareButton.setOnClickListener {
            startActivity(Intent(this, ComparisonActivity::class.java))
        }

        // Save to Room
        if (reviewText.isNullOrBlank()) {
            saveResultButton.isEnabled = false
        }
        saveResultButton.setOnClickListener {
            val text = reviewText
            if (text.isNullOrBlank()) {
                Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val repo = ReviewRepository(applicationContext)
            lifecycleScope.launch {
                try {
                    val id = withContext(Dispatchers.IO) {
                        repo.save(text, sentiment, confidence)
                    }
                    Toast.makeText(this@ResultActivity, "Saved (#$id)", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@ResultActivity, "Failed to save", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

