package com.tucugo.app.data.models

import java.util.Date

/**
 * Modelo de Usuario optimizado.
 * Se eliminaron propiedades no utilizadas: favoriteStores, badges, blockedUsers, tripCount.
 */
data class User(
    val id: String = "",
    val cedula: String = "",
    val photoCedulaUrl: String? = null,
    val photoUrl: String? = null,
    val phoneNumber: String = "",
    val email: String = "",
    val passwordHash: String = "",
    val role: String = "customer", // 'driver', 'moto-taxi', 'delivery', 'business', 'customer'
    val isVerified: Boolean = false,
    val paymentDetails: String? = null,
    val canDeliver: Boolean = false,
    val rating: Double = 0.0,
    val dateOfBirth: Date? = null,
    val isSubscribed: Boolean = false
)
