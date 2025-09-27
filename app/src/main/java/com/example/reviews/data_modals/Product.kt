package com.example.reviews.data_modals

data class Product(
    val id: String,
    val title: String,
    val category: String,
    val price: Double,
    val originalPrice: Double? = null,
    val rating: Double,
    val reviews: Int,
    val imageUrl: String,
    val popularityScore: Double,
    val discount: Int? = null,
    var isFavorite: Boolean = false,
    val description: String = "",
    val features: List<String> = emptyList(),
    val asin: String = ""
)