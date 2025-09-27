package com.example.reviews.data

import android.content.Context
import com.example.reviews.data.db.AppDatabase
import com.example.reviews.data.db.ReviewEntity

class ReviewRepository(private val context: Context) {
    private val dao = AppDatabase.get(context).reviewDao()

    suspend fun save(text: String, sentiment: String, confidence: Float): Long {
        val entity = ReviewEntity(
            text = text,
            sentiment = sentiment,
            confidence = confidence
        )
        return dao.insert(entity)
    }
}
