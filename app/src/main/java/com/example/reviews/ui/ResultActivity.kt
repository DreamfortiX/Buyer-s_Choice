package com.example.reviews.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.reviews.R

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val sentiment = intent.getStringExtra("sentiment") ?: "unknown"
        val confidence = intent.getFloatExtra("confidence", 0f)

        val sentimentText = findViewById<TextView>(R.id.sentimentText)
        val confidencePercent = findViewById<TextView>(R.id.confidencePercent)
        val progressBar = findViewById<ProgressBar>(R.id.confidenceProgress)
        val analyzeAnotherButton = findViewById<Button>(R.id.analyzeAnotherButton)
        val homeButton = findViewById<View>(R.id.homeButton)

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

        // Actions
        analyzeAnotherButton.setOnClickListener {
            // simply finish to go back to previous (e.g., analyze/home)
            onBackPressedDispatcher.onBackPressed()
        }
        homeButton.setOnClickListener {
            // finish all and go back to MainActivity
            finish()
        }
    }
}
