package com.tucugo.app.data.models

data class Business(
    val id: String = "",
    val userId: String = "",
    val rif: String = "",
    val name: String = "",
    val paymentDetails: String? = null,
    val category: String = "", // 'restaurante', 'supermercado', 'farmacia', etc.
    val badges: List<String> = emptyList()
)
