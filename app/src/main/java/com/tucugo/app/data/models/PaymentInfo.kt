package com.tucugo.app.data.models

data class AdminPaymentConfig(
    val bankName: String = "",
    val accountNumber: String = "",
    val accountHolder: String = "",
    val phoneForPagoMovil: String = "",
    val idForPagoMovil: String = "",
    val binanceId: String = "",
    val zinliEmail: String = ""
)

data class PaymentProof(
    val userId: String = "",
    val orderId: String? = null,
    val tripId: String? = null,
    val amount: Double = 0.0,
    val referenceNumber: String = "",
    val paymentMethod: String = "", // "Pago Movil", "Transferencia", "Binance", "Zinli"
    val proofPhotoUrl: String = "",
    val status: String = "pending", // "pending", "approved", "rejected"
    val timestamp: Long = System.currentTimeMillis()
)
