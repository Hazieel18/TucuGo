package com.tucugo.app.utils

/**
 * Constantes activas del sistema TucuGo.
 * Se eliminaron valores no utilizados de comisiones específicas y costos de prueba.
 */
object Constants {
    // API Keys
    const val MAPTILER_API_KEY = "2IgsvDh7ro6LC9rPuG1T"

    // Comisiones de Operación
    const val COMMISSION_DEFAULT_BUSINESS = 0.04 // 4% por venta
    const val COMMISSION_TRIP = 0.07             // 7% por viaje
    const val COMMISSION_DELIVERY = 0.07         // 7% por entrega

    // Estados de Pedido / Viaje
    const val STATUS_PENDING = "pending"
    const val STATUS_PREPARING = "preparing"
    const val STATUS_READY = "ready_for_pickup"
    const val STATUS_EN_ROUTE = "en_route"
    const val STATUS_DELIVERED = "delivered"
    const val STATUS_CANCELED = "canceled"
}
