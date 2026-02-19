package com.tucugo.app.data.repository

import com.tucugo.app.data.models.AdminIncomeSummary
import com.tucugo.app.data.models.AdminPaymentConfig
import com.tucugo.app.data.models.SystemSettings
import com.tucugo.app.data.models.Vehicle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getIncomeSummary(): Flow<AdminIncomeSummary> = callbackFlow {
        val subscription = db.collection("orders")
            .whereEqualTo("status", "delivered")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    var adminComm = 0.0
                    var gross = 0.0
                    var driverPay = 0.0
                    var businessPay = 0.0

                    for (doc in snapshot.documents) {
                        val total = doc.getDouble("totalAmount") ?: 0.0
                        val type = doc.getString("type") ?: "delivery"
                        gross += total
                        if (type == "ride") {
                            val commission = total * 0.07 // 7% comisión
                            adminComm += commission
                            driverPay += (total - commission)
                        } else {
                            val commission = total * 0.10 // 10% comisión comercios
                            adminComm += commission
                            businessPay += (total - commission)
                        }
                    }
                    trySend(AdminIncomeSummary(gross, adminComm, businessPay, driverPay))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveSystemSettings(settings: SystemSettings): Result<Unit> {
        return try {
            db.collection("system_settings").document("bcv").set(settings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPaymentConfig(): AdminPaymentConfig? {
        return try {
            db.collection("system_settings").document("payments")
                .get().await().toObject(AdminPaymentConfig::class.java)
        } catch (e: Exception) { null }
    }

    fun getPendingVehicleVerifications(): Flow<List<Vehicle>> = callbackFlow {
        val subscription = db.collection("vehicles")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let { trySend(it.toObjects(Vehicle::class.java)) }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun verifyVehicle(driverId: String, approve: Boolean): Result<Unit> {
        return try {
            val status = if (approve) "approved" else "rejected"
            db.runTransaction { transaction ->
                val vehicleRef = db.collection("vehicles").document(driverId)
                val userRef = db.collection("users").document(driverId)
                transaction.update(vehicleRef, "status", status)
                if (approve) {
                    transaction.update(userRef, "isVerified", true)
                    transaction.update(userRef, "role", "driver")
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
