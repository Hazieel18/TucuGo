package com.tucugo.app.data.models

import java.util.Date

data class Delivery(
    val id: String = "",
    val orderId: String = "",
    val driverId: String = "",
    val pickupLocation: Location = Location(),
    val dropoffLocation: Location = Location(),
    val startTime: Date? = null,
    val endTime: Date? = null,
    val status: String = "pending", // 'pending', 'accepted', 'in progress', 'delivered', 'canceled'
    val deliveryFee: Double = 0.0,
    val negotiatedFee: Double? = null,
    val driverLocation: Location? = null // Actualizado a Location para evitar redundancia
)
