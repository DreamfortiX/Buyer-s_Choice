package com.example.reviews.data

data class SentimentResult(
    val sentiment: String,
    val confidence: Float
)

data class Product(
    val id: String,
    val name: String
)

data class ProductComparison(
    val productId: String,
    val productName: String,
    val positivePercentage: Int,
    val neutralPercentage: Int,
    val negativePercentage: Int
)

data class SummaryData(
    val overallSentiment: String,
    val pros: List<String>,
    val cons: List<String>,
    val frequentWords: List<String>
)
