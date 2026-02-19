package com.tucugo.app.data.models

data class Vehicle(
    val driverId: String = "",
    val type: String = "", // "Taxi", "Moto-Taxi"
    val brand: String = "",
    val model: String = "",
    val year: Int = 0,
    val capacity: Int = 0, // Passengers
    val plateNumber: String = "",
    val photoUrls: List<String> = emptyList(),
    val status: String = "pending", // "pending", "approved", "rejected"
    val timestamp: Long = System.currentTimeMillis()
)
