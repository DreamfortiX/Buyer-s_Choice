package com.example.reviews.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

// Request/Response models for Flask /analyze

data class AnalyzeRequest(
    @SerializedName("review_text") val reviewText: String
)

data class AnalyzeResponse(
    @SerializedName("sentiment") val sentiment: String,
    @SerializedName("confidence") val confidence: Float,
    // Optional: server may include human-readable classification
    @SerializedName("classification") val classification: String? = null,
    // Optional: server message
    @SerializedName("message") val message: String? = null,
    // Backward-compat optional distribution in percentages, if server provides it
    @SerializedName("distribution") val distribution: Map<String, Int>? = null
)

// Request/Response models for Flask /compare
data class CompareRequest(
    @SerializedName("product_ids") val productIds: List<String>
)

data class ProductSentiment(
    @SerializedName("product_id") val productId: String,
    @SerializedName("product_name") val productName: String,
    // Map percentage fields from backend into our existing model names
    @SerializedName(value = "positive_percentage", alternate = ["positive_percent"]) val positive: Int,
    @SerializedName(value = "neutral_percentage", alternate = ["neutral_percent"]) val neutral: Int,
    @SerializedName(value = "negative_percentage", alternate = ["negative_percent"]) val negative: Int,
    // Optionally available
    @SerializedName("total_reviews") val totalReviews: Int? = null,
    @SerializedName("overall_sentiment") val overallSentiment: String? = null
)

data class CompareResponse(
    @SerializedName("products") val products: List<ProductSentiment>,
    @SerializedName(value = "top_product", alternate = ["best_product"]) val topProduct: String? = null,
    @SerializedName("comparison_date") val comparisonDate: String? = null
)

interface ApiService {
    @POST("/analyze")
    suspend fun analyze(@Body body: AnalyzeRequest): AnalyzeResponse

    @POST("/compare")
    suspend fun compare(@Body body: CompareRequest): CompareResponse

    // Summarize endpoint for a single product (POST)
    data class SummarizeRequest(
        @SerializedName("product_id") val productId: String
    )

    data class WordFrequency(
        @SerializedName("word") val word: String,
        @SerializedName("frequency") val frequency: Int
    )

    data class SentimentBreakdown(
        @SerializedName("positive") val positive: Float,
        @SerializedName("negative") val negative: Float,
        @SerializedName("neutral") val neutral: Float
    )

    data class SummarizeResponse(
        @SerializedName("product_id") val productId: String? = null,
        @SerializedName("product_name") val productName: String? = null,
        @SerializedName(value = "total_reviews_analyzed", alternate = ["total_reviews"]) val totalReviewsAnalyzed: Int? = null,
        @SerializedName("summary") val summary: String,
        @SerializedName("key_insights") val keyInsights: List<String> = emptyList(),
        @SerializedName("word_cloud") val wordCloud: List<WordFrequency> = emptyList(),
        @SerializedName("sentiment_breakdown") val sentimentBreakdown: SentimentBreakdown? = null,
        @SerializedName("summary_generated_at") val summaryGeneratedAt: String? = null
    )

    @POST("/summarize")
    suspend fun summarize(@Body body: SummarizeRequest): SummarizeResponse
}

