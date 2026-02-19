package com.tucugo.app.data.models

import java.util.Date

/**
 * Modelos de soporte optimizados.
 * Se eliminaron propiedades redundantes de la solicitud de verificación
 * que ya están cubiertas por los modelos de Negocio y Vehículo.
 */

data class VerificationRequest(
    val userId: String = "",
    val role: String = "",
    val status: String = "pending",
    val createdAt: Date? = null,
    val businessName: String? = null,
    val businessRif: String? = null,
    val vehicleMake: String? = null,
    val vehicleModel: String? = null
)

data class SystemSettings(
    val id: String = "bcv",
    val rate: Double = 0.0,
    val lastUpdated: Date? = null
)
