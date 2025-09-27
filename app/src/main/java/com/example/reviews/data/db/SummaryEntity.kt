package com.example.reviews.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "summaries")
data class SummaryEntity(
    @PrimaryKey val productId: String,
    val productName: String,
    val summaryText: String,
    val wordCloudUrl: String,
    val createdAt: Long
)
