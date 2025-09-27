package com.example.reviews.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val sentiment: String,
    val confidence: Float,
    val createdAt: Long = System.currentTimeMillis()
)
