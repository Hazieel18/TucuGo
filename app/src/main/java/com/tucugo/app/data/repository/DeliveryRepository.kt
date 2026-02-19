package com.tucugo.app.data.repository

import com.tucugo.app.data.models.Delivery
import com.tucugo.app.data.models.Location
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import java.util.Date

class DeliveryRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val deliveriesCollection = db.collection("deliveries")
    private val ordersCollection = db.collection("orders")

    /**
     * El repartidor acepta una entrega vinculada a un pedido.
     */
    suspend fun acceptDelivery(orderId: String, driverId: String, fee: Double): Result<String> {
        return try {
            val docRef = deliveriesCollection.document()
            val delivery = Delivery(
                id = docRef.id,
                orderId = orderId,
                driverId = driverId,
                status = "accepted",
                startTime = Date(),
                deliveryFee = fee
            )
            
            db.runTransaction { transaction ->
                transaction.set(docRef, delivery)
                transaction.update(ordersCollection.document(orderId), "status", "shipping")
            }.await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Escucha actualizaciones de la entrega (especialmente la ubicación del repartidor).
     */
    fun getDeliveryUpdates(deliveryId: String): Flow<Delivery?> = callbackFlow {
        val subscription = deliveriesCollection.document(deliveryId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(Delivery::class.java))
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Actualiza la posición GPS del repartidor para el tracking en vivo.
     */
    fun updateDriverLocation(deliveryId: String, lat: Double, lng: Double) {
        deliveriesCollection.document(deliveryId)
            .update("driverLocation", Location(lat = lat, lng = lng))
    }

    suspend fun completeDelivery(deliveryId: String, orderId: String): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                transaction.update(deliveriesCollection.document(deliveryId), "status", "delivered", "endTime", Date())
                transaction.update(ordersCollection.document(orderId), "status", "delivered")
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
