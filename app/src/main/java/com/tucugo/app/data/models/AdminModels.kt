package com.tucugo.app.data.models

data class AdminIncomeSummary(
    val totalGrossRevenue: Double = 0.0,    // Todo el dinero que ha entrado al sistema
    val totalAdminCommissions: Double = 0.0, // Tu ganancia neta (7% de viajes, etc.)
    val totalBusinessPayouts: Double = 0.0,  // Dinero que debes pagar a comercios
    val totalDriverPayouts: Double = 0.0,    // Dinero que debes pagar a conductores
    val totalPrimeSubscriptions: Double = 0.0 // Ingresos por TucuGo Prime
)
