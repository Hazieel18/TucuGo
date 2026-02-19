package com.tucugo.app.utils

import com.tucugo.app.data.models.SystemSettings
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.net.URL
import java.util.Scanner

/**
 * Utilidad híbrida para la conversión de precios USD -> VES.
 * Prioriza la tasa manual configurada por el Admin en Firebase, 
 * pero puede consultar una API externa si se solicita.
 */
object CurrencyUtils {
    private var currentRate: Double = 36.5 

    /**
     * PASO 1: Obtiene la tasa manual establecida por el Administrador en Firestore.
     * Esta es la tasa oficial que rige la aplicación.
     */
    suspend fun fetchCurrentRate() {
        try {
            val document = FirebaseFirestore.getInstance()
                .collection("system_settings")
                .document("bcv")
                .get()
                .await()
            
            val settings = document.toObject(SystemSettings::class.java)
            settings?.let {
                currentRate = it.rate
            }
        } catch (e: Exception) {
            // Si falla Firestore, mantenemos la última tasa conocida
        }
    }

    /**
     * PASO 2: (Opcional/Fallo) Consulta una API externa de respaldo en internet.
     * Útil para cuando el Admin no ha actualizado la tasa manualmente.
     */
    suspend fun fetchExternalRate(): Double? {
        return try {
            val url = URL("https://open.er-api.com/v6/latest/USD")
            val scanner = Scanner(url.openStream())
            val response = scanner.useDelimiter("\\A").next()
            val json = JSONObject(response)
            val rates = json.getJSONObject("rates")
            rates.getDouble("VES")
        } catch (e: Exception) {
            null
        }
    }

    fun usdToVes(amountUsd: Double): Double = amountUsd * currentRate
    fun formatVes(amountUsd: Double): String = "Bs. ${String.format("%.2f", usdToVes(amountUsd))}"
    fun getRate(): Double = currentRate
}
