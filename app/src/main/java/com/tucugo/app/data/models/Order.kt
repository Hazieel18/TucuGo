package com.tucugo.app.data.models

import java.util.Date

/**
 * Modelo de Pedido optimizado y unificado.
 */
data class Order(
    val id: String = "",
    val userId: String = "",
    val businessId: String = "",
    val orderDate: Date? = null,
    val status: String = "pending",
    val totalAmount: Double = 0.0, // Monto total cobrado al cliente
    val deliveryAddress: Location = Location(), // Usando Location unificado
    val deliveryFee: Double = 0.0,
    val paymentMethod: String = "",
    val paymentProofUrl: String? = null,
    val paymentReference: String? = null,
    val isDelivery: Boolean = true,
    val salesCommission: Double = 0.0,    // Tu ganancia por la venta (Admin)
    val deliveryCommission: Double = 0.0  // Tu ganancia por el envío (Admin)
)

data class OrderItem(
    val id: String = "",
    val orderId: String = "",
    val productId: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val weight: Double? = null
)
