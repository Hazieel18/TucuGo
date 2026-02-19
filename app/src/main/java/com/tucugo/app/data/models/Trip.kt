package com.tucugo.app.data.models

import java.util.Date

data class Trip(
    val id: String = "",
    val userId: String = "",
    val driverId: String? = null,
    val startLocation: Location = Location(),
    val endLocation: Location = Location(),
    val startTime: Date? = null,
    val endTime: Date? = null,
    val status: String = "pending", // 'pending', 'accepted', 'in_progress', 'completed', 'canceled'
    val fare: Double = 0.0,
    val negotiatedFare: Double? = null,
    val driverCounterOffer: Double? = null,
    val tripType: String = "taxi", // 'taxi', 'moto-taxi'
    val driverLocation: Location? = null, // Usando el modelo Location unificado
    val paymentReference: String? = null
)
