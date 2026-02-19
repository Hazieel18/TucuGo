package com.tucugo.app.data.models

import java.util.Date

data class InterstateTrip(
    val id: String = "",
    val userId: String = "",
    val driverId: String? = null,
    val startLocation: Location = Location(),
    val endLocation: Location = Location(),
    val departureDateTime: Date? = null,
    val isReturnTrip: Boolean = false,
    val returnDateTime: Date? = null,
    val passengerCount: Int = 1,
    val status: String = "pending_driver_acceptance",
    val fare: Double = 0.0,
    val negotiatedFare: Double? = null,
    val driverCounterOffer: Double? = null,
    val finalFare: Double? = null,
    val paymentReference: String? = null
)
