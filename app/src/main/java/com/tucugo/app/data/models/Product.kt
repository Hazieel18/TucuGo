package com.tucugo.app.data.models

data class Product(
    val id: String = "",
    val businessId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val photosUrl: List<String> = emptyList(),
    val preparationTime: Int = 0,
    val isAvailable: Boolean = true,
    val requiresWeightConfirmation: Boolean = false
)
