package com.example.reviews.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

val productList: List<Product> = listOf(
    Product("p1", "Echo Dot (5th Gen)"),
    Product("p2", "Kindle Paperwhite"),
    Product("p3", "Fire TV Stick 4K"),
    Product("p4", "Apple AirPods Pro"),
    Product("p5", "Samsung Galaxy Watch")
)

fun analyzeSentiment(text: String, onResult: (SentimentResult) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        delay(800)
        val positiveWords = listOf("amazing", "great", "love", "excellent", "fast")
        val negativeWords = listOf("bad", "terrible", "poor", "slow", "hate")
        val lower = text.lowercase()
        val score = when {
            positiveWords.any { lower.contains(it) } && !negativeWords.any { lower.contains(it) } -> 0.9f
            negativeWords.any { lower.contains(it) } && !positiveWords.any { lower.contains(it) } -> -0.8f
            else -> 0.1f
        }
        val sentiment = when {
            score > 0.2f -> "positive"
            score < -0.2f -> "negative"
            else -> "neutral"
        }
        val confidence = (0.6f + Random.nextFloat() * 0.4f)
        onResult(SentimentResult(sentiment, confidence))
    }
}

fun compareProducts(ids: List<String>, onResult: (List<ProductComparison>) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        delay(900)
        val results = ids.map { id ->
            val p = productList.first { it.id == id }
            val pos = Random.nextInt(50, 90)
            val neg = Random.nextInt(5, 25)
            val neu = 100 - pos - neg
            ProductComparison(
                productId = id,
                productName = p.name,
                positivePercentage = pos,
                neutralPercentage = neu.coerceAtLeast(0),
                negativePercentage = neg
            )
        }
        onResult(results)
    }
}

fun generateSummary(productId: String, onResult: (SummaryData) -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        delay(1000)
        val overall = listOf("positive", "neutral", "negative").random()
        val pros = listOf(
            "Great battery life",
            "High-quality sound",
            "Fast performance",
            "Excellent build quality"
        ).shuffled().take(3)
        val cons = listOf(
            "A bit pricey",
            "Occasional connectivity issues",
            "Limited color options",
            "Average camera performance"
        ).shuffled().take(3)
        val words = listOf("battery", "sound", "performance", "quality", "price", "design", "comfort", "durable").shuffled()
        onResult(
            SummaryData(
                overallSentiment = overall,
                pros = pros,
                cons = cons,
                frequentWords = words
            )
        )
    }
}
