package com.tucugo.app.data.models

/**
 * Modelo único de ubicación para todo el sistema TucuGo.
 * Se eliminó la clase LatLng por redundancia.
 */
data class Location(
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)
