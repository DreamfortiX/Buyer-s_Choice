package com.example.reviews.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.reviews.R

class ReviewDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_detail)

        val sentiment = intent.getStringExtra("sentiment") ?: "unknown"
        val confidence = intent.getFloatExtra("confidence", 0f)
        val text = intent.getStringExtra("text") ?: ""
        val createdAt = intent.getLongExtra("createdAt", 0L)

        val tvSentiment = findViewById<TextView>(R.id.detailSentiment)
        val tvConfidence = findViewById<TextView>(R.id.detailConfidence)
        val tvText = findViewById<TextView>(R.id.detailText)
        val tvTimestamp = findViewById<TextView>(R.id.detailTimestamp)

        val lower = sentiment.lowercase()
        tvSentiment.text = lower.uppercase()
        val colorRes = when (lower) {
            "positive" -> R.color.positive_sentiment
            "negative" -> R.color.negative_sentiment
            else -> R.color.neutral_sentiment
        }
        tvSentiment.setTextColor(ContextCompat.getColor(this, colorRes))

        val percent = (confidence.coerceIn(0f, 1f) * 100).toInt()
        tvConfidence.text = "$percent%"

        tvText.text = text

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        tvTimestamp.text = sdf.format(java.util.Date(createdAt))

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.detailToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.history_title)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}
